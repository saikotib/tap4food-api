package com.endeavour.tap4food.app.request.dto;

import java.util.List;

import lombok.Data;

@Data
public class OfferSuggestionItemRequest {

	private String description;
	
	private List<SuggestItem> suggestionItems;
	
	private String customerSpecification;
	
	private String buttonType;
	
	@Data
	public static class SuggestItem{
		
		private String itemName;
		
		private Long itemId;
	}
}
