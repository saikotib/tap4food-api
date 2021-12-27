package com.endeavour.tap4food.app.request.dto;

import java.util.List;

import lombok.Data;

@Data
public class OfferFoodItemsListRequest {

	private String description;
	
	private List<Item> foodItems;
	
	private String customerSpecification;
	
	private String buttonType;
	
	@Data
	public static class Item {
		
		private String itemName;
		
		private Long itemId;
		
		private Double actualPrice;
		
		private Double offerPrice;
		
		private boolean customizationFlag;
		
		private long quantity;
	}
	
}
