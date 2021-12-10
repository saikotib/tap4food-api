package com.endeavour.tap4food.app.response.dto;

import lombok.Data;

@Data
public class FoodCourtResponse {

	private Long foodCourtId;
	
	private String foodCourtName;
	
	private String buName;
	
	private Long buId;
	
	private String buType;
	
	private String address;
	
	private String city;
	
	private String country;
	
	private String state;
}
