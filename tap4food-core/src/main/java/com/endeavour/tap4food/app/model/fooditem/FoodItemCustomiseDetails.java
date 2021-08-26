package com.endeavour.tap4food.app.model.fooditem;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_FOOD_ITEM_CUSTOMISATION)
public class FoodItemCustomiseDetails {

	@Id
	private String id;

	private Long foodItemId;

	private String foodItemName;
	
	private Long foodStallId;

	private String foodItemDescription;

	private List<String> customiseTypes;

	private List<String> customiseFoodItems;

	private List<String> customiseFoodItemsDescriptions;

	private List<String> customiseFoodItemsSelectButtons;

	private List<String> customiseFoodItemsCustomerSpecifications;

	private String addOnDescription;

	private List<String> addOnItemsIds;

	private String addOnSelectButton;

	private String addOnCustomerSpecification;

	@JsonIgnore
	private String requestId;
}
