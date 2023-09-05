package com.endeavour.tap4food.app.response.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class SettlementTransaction {
	
	 @JsonProperty("TXNID")
	    private String TXNID;

	    @JsonProperty("TXNTYPE")
	    private String TXNTYPE;

	    @JsonProperty("TXNDATE")
	    private String TXNDATE;

	    @JsonProperty("TXNAMOUNT")
	    private String TXNAMOUNT;

	    @JsonProperty("CUSTID")
	    private String CUSTID;

	    @JsonProperty("PAYMENTMODE")
	    private String PAYMENTMODE;

	    @JsonProperty("MID")
	    private String MID;

	    @JsonProperty("MERCHANTNAME")
	    private String MERCHANTNAME;

	    @JsonProperty("ORDERID")
	    private String ORDERID;

	    @JsonProperty("UTRPROCESSEDTIME")
	    private String UTRPROCESSEDTIME;

	    @JsonProperty("COMMISSION")
	    private String COMMISSION;

 
	    @JsonProperty("GST")
	    private String GST; // Add the GST property

	    @JsonProperty("PAYOUT DATE")
	    private String PAYOUTDATE; // Add the PAYOUT DATE property

	    @JsonProperty("SETTLED DATE")
	    private String SETTLEDDATE; // Add the SETTLED DATE property

	    @JsonProperty("SETTLEDAMOUNT")
	    private String SETTLEDAMOUNT; // Add the SETTLEDAMOUNT property

	    @JsonProperty("UTR")
	    private String UTR;
	    @JsonProperty("BANKNAME")
	    private String BANKNAME;
	    
    public SettlementTransaction(String tXNID, String tXNTYPE, String tXNDATE, String tXNAMOUNT, String cUSTID,
				String pAYMENTMODE, String mID, String mERCHANTNAME, String oRDERID, String uTRPROCESSEDTIME,
				String cOMMISSION, String gST, String pAYOUTDATE, String sETTLEDDATE, String sETTLEDAMOUNT, String uTR,
				String bANKNAME) {
			super();
			TXNID = tXNID;
			TXNTYPE = tXNTYPE;
			TXNDATE = tXNDATE;
			TXNAMOUNT = tXNAMOUNT;
			CUSTID = cUSTID;
			PAYMENTMODE = pAYMENTMODE;
			MID = mID;
			MERCHANTNAME = mERCHANTNAME;
			ORDERID = oRDERID;
			UTRPROCESSEDTIME = uTRPROCESSEDTIME;
			COMMISSION = cOMMISSION;
			GST = gST;
			PAYOUTDATE = pAYOUTDATE;
			SETTLEDDATE = sETTLEDDATE;
			SETTLEDAMOUNT = sETTLEDAMOUNT;
			UTR = uTR;
			BANKNAME = bANKNAME;
		}
	public String getBANKNAME() {
			return BANKNAME;
		}
		public void setBANKNAME(String bANKNAME) {
			BANKNAME = bANKNAME;
		}
	public SettlementTransaction() {
		super();
	}
	public SettlementTransaction(String tXNID, String tXNTYPE, String tXNDATE, String tXNAMOUNT, String cUSTID,
			String pAYMENTMODE, String mID, String mERCHANTNAME, String oRDERID, String uTRPROCESSEDTIME,
			String cOMMISSION, String gST, String pAYOUTDATE, String sETTLEDDATE, String sETTLEDAMOUNT, String uTR) {
		super();
		TXNID = tXNID;
		TXNTYPE = tXNTYPE;
		TXNDATE = tXNDATE;
		TXNAMOUNT = tXNAMOUNT;
		CUSTID = cUSTID;
		PAYMENTMODE = pAYMENTMODE;
		MID = mID;
		MERCHANTNAME = mERCHANTNAME;
		ORDERID = oRDERID;
		UTRPROCESSEDTIME = uTRPROCESSEDTIME;
		COMMISSION = cOMMISSION;
		GST = gST;
		PAYOUTDATE = pAYOUTDATE;
		SETTLEDDATE = sETTLEDDATE;
		SETTLEDAMOUNT = sETTLEDAMOUNT;
		UTR = uTR;
	}
	public String getTXNID() {
		return TXNID;
	}
	public void setTXNID(String tXNID) {
		TXNID = tXNID;
	}
	public String getTXNTYPE() {
		return TXNTYPE;
	}
	public void setTXNTYPE(String tXNTYPE) {
		TXNTYPE = tXNTYPE;
	}
	public String getTXNDATE() {
		return TXNDATE;
	}
	public void setTXNDATE(String tXNDATE) {
		TXNDATE = tXNDATE;
	}
	public String getTXNAMOUNT() {
		return TXNAMOUNT;
	}
	public void setTXNAMOUNT(String tXNAMOUNT) {
		TXNAMOUNT = tXNAMOUNT;
	}
	public String getCUSTID() {
		return CUSTID;
	}
	public void setCUSTID(String cUSTID) {
		CUSTID = cUSTID;
	}
	public String getPAYMENTMODE() {
		return PAYMENTMODE;
	}
	public void setPAYMENTMODE(String pAYMENTMODE) {
		PAYMENTMODE = pAYMENTMODE;
	}
	public String getMID() {
		return MID;
	}
	public void setMID(String mID) {
		MID = mID;
	}
	public String getMERCHANTNAME() {
		return MERCHANTNAME;
	}
	public void setMERCHANTNAME(String mERCHANTNAME) {
		MERCHANTNAME = mERCHANTNAME;
	}
	public String getORDERID() {
		return ORDERID;
	}
	public void setORDERID(String oRDERID) {
		ORDERID = oRDERID;
	}
	public String getUTRPROCESSEDTIME() {
		return UTRPROCESSEDTIME;
	}
	public void setUTRPROCESSEDTIME(String uTRPROCESSEDTIME) {
		UTRPROCESSEDTIME = uTRPROCESSEDTIME;
	}
	public String getCOMMISSION() {
		return COMMISSION;
	}
	public void setCOMMISSION(String cOMMISSION) {
		COMMISSION = cOMMISSION;
	}
	public String getGST() {
		return GST;
	}
	public void setGST(String gST) {
		GST = gST;
	}
	public String getPAYOUTDATE() {
		return PAYOUTDATE;
	}
	public void setPAYOUTDATE(String pAYOUTDATE) {
		PAYOUTDATE = pAYOUTDATE;
	}
	public String getSETTLEDDATE() {
		return SETTLEDDATE;
	}
	public void setSETTLEDDATE(String sETTLEDDATE) {
		SETTLEDDATE = sETTLEDDATE;
	}
	public String getSETTLEDAMOUNT() {
		return SETTLEDAMOUNT;
	}
	public void setSETTLEDAMOUNT(String sETTLEDAMOUNT) {
		SETTLEDAMOUNT = sETTLEDAMOUNT;
	}
	public String getUTR() {
		return UTR;
	}
	public void setUTR(String uTR) {
		UTR = uTR;
	}
	

    // Getters and Setters
}