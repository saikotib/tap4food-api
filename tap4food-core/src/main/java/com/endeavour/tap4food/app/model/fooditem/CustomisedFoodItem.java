package com.endeavour.tap4food.app.model.fooditem;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_FOOD_ITEM_CUSTOMISATION)
public class CustomisedFoodItem {

	@Id
	private Long id;
	
	private Long foodItemId;
	
	private String foodItemName;
	
	private String itemDescription;
	
	private String category;
	
	private String subCategory;
	
	private String variant;
	
	private String extraVariant;
	
	private Double price;
	
	@JsonIgnore
	private String requestId;
}
