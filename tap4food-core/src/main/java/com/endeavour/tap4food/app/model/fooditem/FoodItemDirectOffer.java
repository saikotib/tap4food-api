package com.endeavour.tap4food.app.model.fooditem;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class FoodItemDirectOffer {

	@Id
	private String id;
	
	private Long foodItemId;
	
	private String foodItemName;
	
	private String custimizeType;
	
	private Double actualPrice;
	
	private Double offerPrice;
	
	private Long quantity;
}
