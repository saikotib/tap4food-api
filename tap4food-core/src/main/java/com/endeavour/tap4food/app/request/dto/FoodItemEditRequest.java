package com.endeavour.tap4food.app.request.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FoodItemEditRequest {

	private Long foodStallId;
	
	private Long foodItemId;
	
	private String foodItemName;
	
	private String category;
	
	private String subCategory;
	
	private String description;
	
	private String cuisine;
	
	private boolean customizationFlag;
	
	@JsonProperty("isPizaa")
	private boolean isPizza;
	
	private boolean addOnFlag;
	
	private boolean vegFlag;
	
	private boolean eggFlag;
	
	private boolean recomendedFlag;
	
	private List<String> customizationTypes;
	
	private List<String> customiseFoodItems;

	private List<String> customiseFoodItemsDescriptions;

	private List<String> customiseFoodItemsSelectButtons;

	private List<String> customiseFoodItemsCustomerSpecifications;

	private String addOnDescription;

	private List<String> addOnItemsIds;

	private String addOnSelectButton;

	private String addOnCustomerSpecification;
	
	private String taxType;
}
