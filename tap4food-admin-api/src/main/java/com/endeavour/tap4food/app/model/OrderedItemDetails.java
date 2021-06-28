package com.endeavour.tap4food.app.model;

import lombok.Data;

@Data
public class OrderedItemDetails {

	private String itemName;
	
	private Integer quantity;
	
	private Double amount;
	
	private Long orderNumber;
	
}
