package com.endeavour.tap4food.app.model;

import lombok.Data;

@Data
public class CartItem {

	private long foodItemId;
	
	private String itemName;
	
	private String customerId;
	
	private double itemCost;
	
	private double tax;
	
	private double subTotalAmount;
	
}
