package com.kngxscn.dnsrelay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DNSRelayServer {
    private static Map<String, String> domainIpMap;
    private static DatagramSocket socket;

    static final Object lockObj = new Object();

    static Map<String, String> getDomainIpMap() {
        return domainIpMap;
    }

    static DatagramSocket getSocket() {
        return socket;
    }

    private static Map<String, String> generateDomainIpMap(String filePath) {
        // 读取本地域名-IP映射文件的内容
        File localTableFile = new File(filePath);
        Map<String, String> domainIpMap = new HashMap<String, String>();
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return domainIpMap;
    }

    public static void main(String[] args ) {
        if (args.length < 1) {
            System.out.println("请输入本地域名-IP映射文件的路径");
        }

        domainIpMap = generateDomainIpMap(args[0]);
        System.out.println("本地域名-IP映射文件读取完成。一共" + domainIpMap.size() + "条记录");

        try {
            socket = new DatagramSocket(53);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        ExecutorService servicePool = Executors.newFixedThreadPool(10);  // 容纳10个线程的线程池
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            servicePool.execute(new QueryParser(packet));
        }
    }
}
