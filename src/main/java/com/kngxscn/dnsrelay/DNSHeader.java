package com.kngxscn.dnsrelay;

public class DNSHeader {
	/**
	 * DNS Header
	    0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                      ID                       |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |QR|  opcode   |AA|TC|RD|RA|   Z    |   RCODE   |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    QDCOUNT                    |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    ANCOUNT                    |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    NSCOUNT                    |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    ARCOUNT                    |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 */
	
	/* 会话标识（2字节）*/
	private short transID;

	/* Flags（2字节）*/
	private short flags;
	
	/* QDCOUNT（2字节）*/
	private short qdcount;
	
	/* ANCOUNT（2字节）*/
	private short ancount;
	
	/* NSCOUNT（2字节）*/
	private short nscount;
	
	/* ARCOUNT（2字节）*/
	private short arcount;

	public DNSHeader() {}

	public DNSHeader(short transID, short flags, short qdcount, short ancount, short nscount, short arcount) {
		this.transID = transID;
		this.flags = flags;
		this.qdcount = qdcount;
		this.ancount = ancount;
		this.nscount = nscount;
		this.arcount = arcount;
	}

	public short getTransID() {
		return transID;
	}

	public void setTransID(short transID) {
		this.transID = transID;
	}

	public short getFlags() {
		return flags;
	}

	public void setFlags(short flags) {
		this.flags = flags;
	}

	public short getQdcount() {
		return qdcount;
	}

	public void setQdcount(short qdcount) {
		this.qdcount = qdcount;
	}

	public short getAncount() {
		return ancount;
	}

	public void setAncount(short ancount) {
		this.ancount = ancount;
	}

	public short getNscount() {
		return nscount;
	}

	public void setNscount(short nscount) {
		this.nscount = nscount;
	}

	public short getArcount() {
		return arcount;
	}

	public void setArcount(short arcount) {
		this.arcount = arcount;
	}

    /**
     * 输出包含DNS协议头所有信息的字节数组
     */
    public byte[] toByteArray() {
	    byte[] data = new byte[12];
	    int offset = 0;
	    byte[] byte_2 = new byte[2];
	    byte_2 = Utils.shortToByteArray(transID);
	    for (int i=0; i<2; i++) {
	        data[offset++] = byte_2[i];
        }
        byte_2 = Utils.shortToByteArray(flags);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        byte_2 = Utils.shortToByteArray(qdcount);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        byte_2 = Utils.shortToByteArray(ancount);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        byte_2 = Utils.shortToByteArray(nscount);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        byte_2 = Utils.shortToByteArray(arcount);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        return data;
    }
}
