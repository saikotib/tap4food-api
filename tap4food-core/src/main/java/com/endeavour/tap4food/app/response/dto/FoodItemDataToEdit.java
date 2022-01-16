package com.endeavour.tap4food.app.response.dto;

import java.util.List;
import java.util.Map;

import com.endeavour.tap4food.app.model.fooditem.FoodItem;

import lombok.Data;

@Data
public class FoodItemDataToEdit {

	private FoodItem foodItemDetails;
	
	private boolean customizationFlag;
	
	private List<String> customiseTypes;
	
	private List<CustomizationEntry> customizationEntries;
	
	private Map<String, String> buttons;
	
	private Map<String, String> descriptions;
	
	private Map<String, String> customerSpecifications;
	
	private List<FoodItem> addOnItems;
	
	@Data
	public static class CustomizationEntry{
		
		private String key;
		
		private List<String> values;
	}
}
