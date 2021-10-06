package com.endeavour.tap4food.app.model.offer;

import lombok.Data;

@Data
public class OfferFoodItem {

	private Long offerId;
	
	private String offerName;
	
	private String foodItemName;
	
	private String customizeType;
	
	private Double actualPrice;
	
	private Double offerPrice;
	
	private String list;
}
