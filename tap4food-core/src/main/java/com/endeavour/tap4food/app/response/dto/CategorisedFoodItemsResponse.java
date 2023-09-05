package com.endeavour.tap4food.app.response.dto;

import java.util.List;

import lombok.Data;

@Data
public class CategorisedFoodItemsResponse {

	private String category;
	
	private List<FoodItemResponse> items;
}
