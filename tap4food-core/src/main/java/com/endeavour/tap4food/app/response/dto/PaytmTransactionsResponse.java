package com.endeavour.tap4food.app.response.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class PaytmTransactionsResponse {
	
	
	@JsonProperty("settlementListResponse")
    private SettlementListResponse settlementListResponse;

    @JsonProperty("status")
    private String status;

    @JsonProperty("count")
    private int count;

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("errorMessage")
    private String errorMessage;
    
    
    
	public PaytmTransactionsResponse() {
		super();
	}
	public PaytmTransactionsResponse(SettlementListResponse settlementListResponse, String status, int count, String resultCode,
			String errorMessage) {
		super();
		this.settlementListResponse = settlementListResponse;
		this.status = status;
		this.count = count;
		this.resultCode = resultCode;
		this.errorMessage = errorMessage;
	}
	public SettlementListResponse getSettlementListResponse() {
		return settlementListResponse;
	}
	public void setSettlementListResponse(SettlementListResponse settlementListResponse) {
		this.settlementListResponse = settlementListResponse;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getResultCode() {
		return resultCode;
	}
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
    

    // Getters and Setters
}