package com.endeavour.tap4food.app.response.dto;

import lombok.Data;

@Data
public class MerchantResponseDto {

	private Long merchantId;
	
	private String stallName;
	
	private String ownerName;
	
	private String address;
	
	private String phoneNumber;
	
	private String status;
	
	private String registeredDate;
}
