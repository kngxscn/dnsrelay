package com.kngxscn.dnsrelay;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple DNSRelayServer.
 */
public class DNSRelayServerTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DNSRelayServerTest(String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( DNSRelayServerTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue( true );
    }
}
