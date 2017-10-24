package com.kngxscn.dnsrelay;

public class Utils {
    /**
     * 一维字节数组转 short 值(2 字节)
     */
    public static short byteArrayToShort(byte[] b){
        return byteArrayToShort(b, 0);
    }

    /**
     * 一维字节数组转 short 值(2 字节)
     */
    public static short byteArrayToShort(byte[] b,int offset){
        return (short) (((b[offset] & 0xff) << 8) | (b[offset + 1] & 0xff));
    }

    /**
     * 将 short 类型数据转为 byte[]
     */
    public static byte[] shortToByteArray(short i) {
        byte[] result = new byte[2];
        //由高位到低位
        result[0] = (byte)((i >> 8) & 0xFF);
        result[1] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * byte 转 int
     */
    public static int byteToInt (byte b) {
        return (b & 0xff);
    }

    /**
     * 一维字节数组转 int 值(4 字节)
     */
    public static int byteArrayToInt(byte[] b){
        return byteArrayToInt(b, 0);
    }

    /**
     * 一维字节数组转 int 值(4 字节)
     */
    public static int byteArrayToInt(byte[] bytes, int offset){
        int value= 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

    /**
     * 将 int 类型数据转为 byte[]
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * byte 转为 16 进制字符串
     */
    public static String byteToHexString (byte b) {
        return Integer.toHexString(byteToInt(b));
    }

    /**
     * byte[] 转化为16进制字符串
     */
    public static String byteArrayToHexString (byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        String s;
        for (byte b: bytes) {
            s = byteToHexString(b);
            if (s.length() < 2) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 一维字节数组转化为Ascii对应的字符串
     */

    public static String byteArrayToAscii(byte[] bytes, int offset, int dataLen) {
        if ((bytes == null) || (bytes.length == 0) || (offset < 0) || (dataLen <= 0)
                || (offset + dataLen > bytes.length)) {
            return null;
        }
        String asciiStr = null;
        byte[] data = new byte[dataLen];
        System.arraycopy(bytes, offset, data, 0, dataLen);
        try {
            asciiStr = new String(data, "ISO8859-1");
        } catch (Exception e) {
            System.out.println("编码异常");
        }
        return asciiStr;
    }

    /**
     * 从字节数组中提取出域名
     */
    public static String extractDomain(byte[] bytes, int offset, int stopByte) {
        StringBuffer stringBuffer = new StringBuffer();
        int subLen = 0;
        while (offset < bytes.length && byteToInt(bytes[offset]) != stopByte) {
            subLen = byteToInt(bytes[offset++]);
            stringBuffer.append(byteArrayToAscii(bytes, offset, subLen));
            offset += subLen;
            if(offset < bytes.length && byteToInt(bytes[offset]) != stopByte) {
                stringBuffer.append(".");
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 域名转化为字节数组
     */
    public static byte[] domainToByteArray(String domain) {
        byte[] result = new byte[domain.length()+2];
        int offset = 0;
        String[] subDomainArray = domain.split("\\.");
        for (String subDomain: subDomainArray) {
            result[offset++] = (byte) subDomain.length();
            for (char c: subDomain.toCharArray()) {
                result[offset++] = (byte)c;
            }
        }
        result[offset++] = 0x00;
        return result;
    }

    /**
     * IPv4点分十进制转换为一维字节数组
     */
    public static byte[] ipv4ToByteArray(String ipv4) {
        byte[] result = new byte[4];
        String[] ipv4SubArray = ipv4.split("\\.");
        if (ipv4SubArray.length != 4) {
            System.out.println("不是合法的IPv4地址");
            return null;
        }
        for (int i=0; i<ipv4SubArray.length; i++) {
            int num = Integer.parseInt(ipv4SubArray[i]);
            byte tmp = 0;
            if (num > 127) {
                tmp = (byte)(num - 256);
            } else {
                tmp = (byte)num;
            }
            result[i] = tmp;
        }
        return result;
    }
}
