package com.kngxscn.dnsrelay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;


public class DNSRelayServer {
    public static void main( String[] args ) throws IOException{
        if (args.length < 1) {
            System.out.println("请输入本地域名-IP映射文件的路径");
        }

        // 读取本地域名-IP映射文件的内容
        File localTableFile = new File(args[0]);
        Map<String, String> domainIpMap = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new FileReader(localTableFile));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] contentList = line.split(" ");
            if (contentList.length < 2) {
                continue;
            }
            domainIpMap.put(contentList[1], contentList[0]);
        }
        br.close();
        System.out.println("本地域名-IP映射文件读取完成");

        DatagramSocket socket = new DatagramSocket(53);
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        // 接受DNS查询请求，处理请求，返回结果
        while (true) {
            socket.receive(packet);

            int offset = 0;
            byte[] buff_2 = new byte[2];
            DNSHeader dnsHeader = new DNSHeader();
            DNSQuestion dnsQuestion = new DNSQuestion();
//            List<DNSQuestion> dnsQuestionList = new ArrayList<DNSQuestion>();
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
                    System.out.println("DNS数据长度不匹配，Malformed Packet");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Malformed Packet");
            }

            // 查询本地域名-IP映射
            String ip = domainIpMap.getOrDefault(dnsQuestion.getQname(), "");
            System.out.println("domain:" + dnsQuestion.getQname() + " ip:" + ip);

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
                System.out.println(Utils.byteArrayToHexString(anDNSRRByteArray));
                // 回复响应数据包
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                DatagramPacket responsePacket = new DatagramPacket(response_data, response_data.length, clientAddress, clientPort);
                socket.send(responsePacket);

            } else { // 本地未检索到，请求因特网DNS服务器
                System.out.println("请求因特网DNS服务器");
                InetAddress dnsServerAddress = InetAddress.getByName("10.3.9.4");
                DatagramPacket internetSendPacket = new DatagramPacket(data, packet.getLength(), dnsServerAddress, 53);
                DatagramSocket internetSocket = new DatagramSocket();
                internetSocket.send(internetSendPacket);
                byte[] receivedData = new byte[1024];
                DatagramPacket internetReceivedPacket = new DatagramPacket(receivedData, receivedData.length);
                internetSocket.receive(internetReceivedPacket);

                // 回复响应数据包
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                DatagramPacket responsePacket = new DatagramPacket(receivedData, internetReceivedPacket.getLength(), clientAddress, clientPort);
                socket.send(responsePacket);
                internetSocket.close();
            }

        }
    }
}
