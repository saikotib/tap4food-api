package com.endeavour.tap4food.app.response.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CustomizationResponse {

	private Long foodItemId;
	
	private List<Option> options;
	
	private Map<String, Map<String, Map<String, Double>>> combinationsMap;
	
	@Data
	public static class Option{
		
		private String key;
		
		private String label;
		
		private List<String> optionItems;
		
		private int order;
		
		private List<Double> prices;
	}
	
}
