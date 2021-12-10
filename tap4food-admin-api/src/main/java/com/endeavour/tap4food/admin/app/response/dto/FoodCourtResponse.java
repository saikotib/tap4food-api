package com.endeavour.tap4food.admin.app.response.dto;

import lombok.Data;

@Data
public class FoodCourtResponse {

	private String country;
	
	private String state;
	
	private String city;
	
	private String buName;
	
	private String foodCourtName;
	
	private Long foodCourtId;
	
	private String qrCodeUrl;
	
	private boolean isQRCodeGenerated;
}
