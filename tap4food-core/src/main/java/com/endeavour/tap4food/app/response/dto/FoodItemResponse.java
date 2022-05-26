package com.endeavour.tap4food.app.response.dto;

import java.util.List;

import org.bson.types.Binary;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FoodItemResponse {

	private String dbId;
	
	private Long foodItemId;
	
	private String foodItemName;
	
	private String description;
	
	private String category;
	
	private String subCategory;
	
	private String cuisine;
	
	private boolean isAddOn;
	
	private boolean isVeg;
	
	private boolean isEgg;
	
	private boolean isReccommended;

	private List<Binary> pic;
	
	private Long foodStallId;
		
	private Double rating;
	
	private long totalReviews;
	
	private Double price;
	
	private String combination;
	
	@JsonProperty("hasCustomizations")
	private boolean hasCustomizations;
	
	private String status;
}
