package com.kngxscn.dnsrelay;

public class DNSQuestion {
	/**
	 * Question 查询字段
		0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                     ...                       |
	  |                    QNAME                      |
	  |                     ...                       |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    QTYPE                      |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    QCLASS                     |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 */
	
	/* QNAME 8bit为单位表示的查询名(广泛的说就是：域名) */
	private String qname;
	
	/* QTYPE（2字节） */
	private short qtype;
	
	/* QCLASS（2字节） */
	private short qclass;
	
	public String getQname() {
		return qname;
	}

	public void setQname(String qname) {
		this.qname = qname;
	}

	public short getQtype() {
		return qtype;
	}

	public void setQtype(short qtype) {
		this.qtype = qtype;
	}

	public short getQclass() {
		return qclass;
	}

	public void setQclass(short qclass) {
		this.qclass = qclass;
	}

    /**
     * 输出包含DNS Question所有信息的字节数组
     */
    public byte[] toByteArray() {
        byte[] data = new byte[qname.length() + 2 + 4];
        int offset = 0;
        byte[] domainByteArray = Utils.domainToByteArray(qname);
        for (int i=0; i<domainByteArray.length; i++) {
            data[offset++] = domainByteArray[i];
        }
        byte[] byte_2 = new byte[2];
        byte_2 = Utils.shortToByteArray(qtype);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        byte_2 = Utils.shortToByteArray(qclass);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        return data;
    }
}
