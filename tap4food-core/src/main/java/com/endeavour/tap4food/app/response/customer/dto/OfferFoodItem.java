package com.endeavour.tap4food.app.response.customer.dto;

import lombok.Data;

@Data
public class OfferFoodItem {

	private Long foodItemId;
	
	private String itemName;
	
	private String combination;
	
	private Double actualPrice;
	
	private Double offerPrice;
	
	private boolean customizationFlag;
	
	private long quantity;
}
