package com.endeavour.tap4food.app.response.dto;

import lombok.Data;

@Data
public class MerchantFoodStall {

	private Long merchantId;
	
	private String foodStallName;
	
	private String owner;
	
	private String phoneNumber;
	
	private String subscriptionDetails;
	
	private String date;
	
	private String status;
}
