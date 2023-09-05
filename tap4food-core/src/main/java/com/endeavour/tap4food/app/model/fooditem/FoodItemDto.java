package com.endeavour.tap4food.app.model.fooditem;

import java.util.List;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FoodItemDto {
	@Id
	private String id;
	
	private Long foodItemId;
	
	private String foodItemName;
	
	private String description;
	
	private String category;
	
	private String subCategory;
	
	private String cuisine;
	
	private boolean isAddOn;
	
	private boolean isVeg;
	
	private boolean isEgg;
	
	private boolean isNonVeg;
	
	private boolean isReccommended;
	
	private boolean isPizza;


	private Long foodStallId;
		
	private Double rating;
	
	private long totalReviews;
	
	private Long baseItem;
	
	private String combination;
	
	@JsonIgnore
	private String requestId;   // This is current timestamp in millisecs sennt by React.
	
	private boolean availableCustomisation;
	
	private List<AddOns> addOns;
	
	private Double price;
	
	private boolean isDefaultCombination;
	
	private String status;
	
	private String taxType;

}
