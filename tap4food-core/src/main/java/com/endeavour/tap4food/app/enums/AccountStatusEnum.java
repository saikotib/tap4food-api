package com.endeavour.tap4food.app.enums;

public enum AccountStatusEnum {

	REQUEST_FOR_APPROVAL("Request for Approval"),
	SENT_FOR_APPROVAL("Sent for Approval"),
	IN_PROGRESS("In progress"),
	APPROVED("Approved"),
	REJECTED("Rejected"),
	ACTIVE("Active"),
	INACTIVE("Inactive"),
	LOCKED("Locked");
	
	private String name;
	
	private AccountStatusEnum(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}
