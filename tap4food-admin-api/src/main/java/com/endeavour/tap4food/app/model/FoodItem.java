package com.endeavour.tap4food.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FoodItem {
	
	private String foodItemName;
	
	private double price;
	
	private double ratingValue;  
	
	private long totalRatings;
	
	private boolean servesVeg;
	
	private boolean servesNonVeg;
	
	private boolean isAvailableNow;

}
