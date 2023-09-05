package com.endeavour.tap4food.user.app.response.dto;

import lombok.Data;

@Data
public class PaytmReqPayload {

	private String mobileNumber;
	
	private String email;
	
	private String totalAmount;
	
	private String orderId;
	
	private String checksum;
}
