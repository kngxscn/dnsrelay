package com.kngxscn.dnsrelay;

import java.io.IOException;
import java.net.*;

public class QueryParser implements Runnable {
    private byte[] data;
    private int dataLength;
    private InetAddress clientAddress;
    private int clientPort;

    QueryParser(DatagramPacket packet) {
        dataLength = packet.getLength();
        data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
        clientAddress = packet.getAddress();
        clientPort = packet.getPort();
    }

    public void run() {
        int offset = 0;
        byte[] buff_2 = new byte[2];
        DNSHeader dnsHeader = new DNSHeader();
        DNSQuestion dnsQuestion = new DNSQuestion();

        // 处理请求，返回结果
        try {
            // 读取DNS协议头
            for (int i = 0; i < 2; i++) {
                buff_2[i] = data[i + offset];
            }
            offset += 2;
            dnsHeader.setTransID(Utils.byteArrayToShort(buff_2));
            for (int i = 0; i < 2; i++) {
                buff_2[i] = data[i + offset];
            }
            offset += 2;
            dnsHeader.setFlags(Utils.byteArrayToShort(buff_2));

            for (int i = 0; i < 2; i++) {
                buff_2[i] = data[i + offset];
            }
            offset += 2;
            dnsHeader.setQdcount(Utils.byteArrayToShort(buff_2));

            for (int i = 0; i < 2; i++) {
                buff_2[i] = data[i + offset];
            }
            offset += 2;
            dnsHeader.setAncount(Utils.byteArrayToShort(buff_2));

            for (int i = 0; i < 2; i++) {
                buff_2[i] = data[i + offset];
            }
            offset += 2;
            dnsHeader.setNscount(Utils.byteArrayToShort(buff_2));

            for (int i = 0; i < 2; i++) {
                buff_2[i] = data[i + offset];
            }
            offset += 2;
            dnsHeader.setArcount(Utils.byteArrayToShort(buff_2));

            // 获取查询的域名

            if (dnsHeader.getQdcount() > 0) { // qdcount通常为1
                String domainName = Utils.extractDomain(data, offset, 0x00);
                dnsQuestion.setQname(domainName);
                offset += domainName.length() + 2;

                for (int j = 0; j < 2; j++) {
                    buff_2[j] = data[j + offset];
                }
                offset += 2;
                dnsQuestion.setQtype(Utils.byteArrayToShort(buff_2));

                for (int j = 0; j < 2; j++) {
                    buff_2[j] = data[j + offset];
                }
                offset += 2;
                dnsQuestion.setQclass(Utils.byteArrayToShort(buff_2));
            } else {
                System.out.println(Thread.currentThread().getName() + " DNS数据长度不匹配，Malformed Packet");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(Thread.currentThread().getName() + " Malformed Packet");
        }

        // 查询本地域名-IP映射
        String ip = DNSRelayServer.getDomainIpMap().getOrDefault(dnsQuestion.getQname(), "");
        System.out.println(Thread.currentThread().getName() + " 本地查找结果 domain:" + dnsQuestion.getQname() + " QTYPE:" + dnsQuestion.getQtype() + " ip:" + ip);

        if (!ip.equals("") && dnsQuestion.getQtype() == 1) { // 在本地域名-IP映射文件中找到结果且查询类型为A(Host Address)，构造回答的数据包
            // Header
            short flags = 0;
            if (!ip.equals("0.0.0.0")) { // rcode为0（没有差错）
                flags = (short) 0x8580;
            } else { // rcode为3（名字差错），只从一个授权名字服务器上返回，它表示在查询中指定的域名不存在
                flags = (short) 0x8583;
            }
            DNSHeader dnsHeaderResponse = new DNSHeader(dnsHeader.getTransID(), flags, dnsHeader.getQdcount(), (short) 1, (short) 1, (short) 0);
            byte[] dnsHeaderByteArray = dnsHeaderResponse.toByteArray();

            // Questions
            byte[] dnsQuestionByteArray = dnsQuestion.toByteArray();

            // Answers
            DNSRR anDNSRR = new DNSRR((short) 0xc00c, dnsQuestion.getQtype(), dnsQuestion.getQclass(), 3600*24, (short) 4, ip);
            byte[] anDNSRRByteArray = anDNSRR.toByteArray();

            // Authoritative nameservers，只是模拟了包格式，nameserver实际指向了查询的域名
            DNSRR nsDNSRR = new DNSRR((short) 0xc00c, (short) 6, dnsQuestion.getQclass(), 3600*24, (short) 0 , null);
            byte[] nsDNSRRByteArray = nsDNSRR.toByteArray();

            byte[] response_data = new byte[dnsHeaderByteArray.length + dnsQuestionByteArray.length + anDNSRRByteArray.length + nsDNSRRByteArray.length];
            int responseOffset = 0;
            for (int i = 0; i < dnsHeaderByteArray.length; i++) {
                response_data[responseOffset++] = dnsHeaderByteArray[i];
            }
            for (int i = 0; i < dnsQuestionByteArray.length; i++) {
                response_data[responseOffset++] = dnsQuestionByteArray[i];
            }
            if (!ip.equals("0.0.0.0")) {
                for (int i = 0; i < anDNSRRByteArray.length; i++) {
                    response_data[responseOffset++] = anDNSRRByteArray[i];
                }
            }
            for (int i = 0; i < nsDNSRRByteArray.length; i++) {
                response_data[responseOffset++] = nsDNSRRByteArray[i];
            }
            System.out.println(Thread.currentThread().getName() + " 响应数据：" +Utils.byteArrayToHexString(anDNSRRByteArray));
            // 回复响应数据包
            DatagramPacket responsePacket = new DatagramPacket(response_data, response_data.length, clientAddress, clientPort);
            synchronized (DNSRelayServer.lockObj) {
                try {
                    System.out.println(Thread.currentThread().getName() + "获得socket，响应" + dnsQuestion.getQname() + ":" + ip);
                    DNSRelayServer.getSocket().send(responsePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else { // 本地未检索到，请求因特网DNS服务器
            System.out.println(Thread.currentThread().getName() + " 请求因特网DNS服务器");
            try {
                InetAddress dnsServerAddress = InetAddress.getByName("114.114.114.114");
                DatagramPacket internetSendPacket = new DatagramPacket(data, dataLength, dnsServerAddress, 53);
                DatagramSocket internetSocket = new DatagramSocket();
                internetSocket.send(internetSendPacket);
                byte[] receivedData = new byte[1024];
                DatagramPacket internetReceivedPacket = new DatagramPacket(receivedData, receivedData.length);
                internetSocket.receive(internetReceivedPacket);

                // 回复响应数据包
                DatagramPacket responsePacket = new DatagramPacket(receivedData, internetReceivedPacket.getLength(), clientAddress, clientPort);
                internetSocket.close();
                synchronized (DNSRelayServer.lockObj) {
                    try {
                        System.out.println(Thread.currentThread().getName() + " 获得socket，响应" + dnsQuestion.getQname());
                        DNSRelayServer.getSocket().send(responsePacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
