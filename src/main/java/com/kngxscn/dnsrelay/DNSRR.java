package com.kngxscn.dnsrelay;

public class DNSRR {
	/**
	 * Answer/Authority/Additional
	   0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |					   ... 						  |
	  |                    NAME                       |
	  |                    ...                        |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    TYPE                       |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    CLASS                      |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    TTL                        |
      |                                               |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    RDLENGTH                   |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    ...                        |
	  |                    RDATA                      |
	  |                    ...                        | 
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 */
	
	/* NAME (2字节 采用消息压缩) */
	private short aname;
	
	/* TYPE（2字节） */
	private short atype;
	
	/* CLASS（2字节） */
	private short aclass;
	
	/* TTL（4字节） */
	private int ttl;
	
	/* RDLENGTH（2字节） */
	private short rdlength;
	
	/* RDATA IPv4为4字节*/
	private String rdata;

	public DNSRR() {}

	public DNSRR(short aname, short atype, short aclass, int ttl, short rdlength, String rdata) {
		this.aname = aname;
		this.atype = atype;
		this.aclass = aclass;
		this.ttl = ttl;
		this.rdlength = rdlength;
		this.rdata = rdata;
	}

	public short getAname() {
		return aname;
	}

	public void setAname(short aname) {
		this.aname = aname;
	}

	public short getAtype() {
		return atype;
	}

	public void setAtype(short atype) {
		this.atype = atype;
	}

	public short getAclass() {
		return aclass;
	}

	public void setAclass(short aclass) {
		this.aclass = aclass;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public short getRdlength() {
		return rdlength;
	}

	public void setRdlength(short rdlength) {
		this.rdlength = rdlength;
	}
	
	public String getRdata() {
		return rdata;
	}

	public void setRdata(String rdata) {
		this.rdata = rdata;
	}

    /**
     * 输出包含DNS RR所有信息的字节数组
     */
    public byte[] toByteArray() {
        byte[] data = new byte[12 + rdlength];
        int offset = 0;
        byte[] byte_2 = new byte[2];
        byte[] byte_4 = new byte[4];
        byte_2 = Utils.shortToByteArray(aname);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        byte_2 = Utils.shortToByteArray(atype);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        byte_2 = Utils.shortToByteArray(aclass);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        byte_4 = Utils.intToByteArray(ttl);
        for (int i=0; i<4; i++) {
            data[offset++] = byte_4[i];
        }
        byte_2 = Utils.shortToByteArray(rdlength);
        for (int i=0; i<2; i++) {
            data[offset++] = byte_2[i];
        }
        if (rdlength == 4) {
            byte_4 = Utils.ipv4ToByteArray(rdata);
            for (int i=0; i<4; i++) {
                data[offset++] = byte_4[i];
            }
        }
        return data;
    }
}
