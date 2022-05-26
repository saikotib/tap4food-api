package com.endeavour.tap4food.app.request.dto;

import java.util.List;

import lombok.Data;

@Data
public class MerchantSearchRequest {

	private String country;
	
	private List<String> states;
	
	private List<String> cities;
	
	private String buType;
}
