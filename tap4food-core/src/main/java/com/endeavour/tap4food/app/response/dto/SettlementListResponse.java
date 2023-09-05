package com.endeavour.tap4food.app.response.dto;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class SettlementListResponse {
	 @JsonProperty("totalCount")
	    private int totalCount;

	    @JsonProperty("settlementTransactionList")
	    private List<SettlementTransaction> settlementTransactionList;

	    @JsonProperty("paginatorPageNum")
	    private int paginatorPageNum;

	    @JsonProperty("paginatorPageSize")
	    private int paginatorPageSize;

	    @JsonProperty("paginatorTotalPage")
	    private int paginatorTotalPage;
    
    
    
    public SettlementListResponse(int totalCount, List<SettlementTransaction> settlementTransactionList,
			int paginatorPageNum, int paginatorPageSize, int paginatorTotalPage, int paginatorTotalCount) {
		super();
		this.totalCount = totalCount;
		this.settlementTransactionList = settlementTransactionList;
		this.paginatorPageNum = paginatorPageNum;
		this.paginatorPageSize = paginatorPageSize;
		this.paginatorTotalPage = paginatorTotalPage;
		this.paginatorTotalCount = paginatorTotalCount;
	}
	public SettlementListResponse() {
		super();
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public List<SettlementTransaction> getSettlementTransactionList() {
		return settlementTransactionList;
	}
	public void setSettlementTransactionList(List<SettlementTransaction> settlementTransactionList) {
		this.settlementTransactionList = settlementTransactionList;
	}
	public int getPaginatorPageNum() {
		return paginatorPageNum;
	}
	public void setPaginatorPageNum(int paginatorPageNum) {
		this.paginatorPageNum = paginatorPageNum;
	}
	public int getPaginatorPageSize() {
		return paginatorPageSize;
	}
	public void setPaginatorPageSize(int paginatorPageSize) {
		this.paginatorPageSize = paginatorPageSize;
	}
	public int getPaginatorTotalPage() {
		return paginatorTotalPage;
	}
	public void setPaginatorTotalPage(int paginatorTotalPage) {
		this.paginatorTotalPage = paginatorTotalPage;
	}
	public int getPaginatorTotalCount() {
		return paginatorTotalCount;
	}
	public void setPaginatorTotalCount(int paginatorTotalCount) {
		this.paginatorTotalCount = paginatorTotalCount;
	}
	private int paginatorTotalCount;

    // Getters and Setters
}