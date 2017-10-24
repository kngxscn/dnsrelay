package com.kngxscn.dnsrelay;

import junit.framework.Assert;
import junit.framework.TestCase;

public class UtilsTest extends TestCase {
    public void testIpv4ToByteArray() {
        byte[] result = Utils.ipv4ToByteArray("22.22.168.222");
        System.out.println(result[0] + " " + result[1] + " " + result[2] + " " + result[3]);
        byte[] respected = {(byte)(22), (byte)(22), (byte)(-88), (byte)(-34)};
        Assert.assertTrue(respected[0] == result[0] && respected[1] == result[1] &&
                respected[2]==result[2] && respected[3] == result[3]);
    }
}
