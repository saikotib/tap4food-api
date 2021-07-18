package com.endeavour.tap4food.app.model.fooditem;

import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_FOOD_ITEM_CUSTOMISETYPE)
public class ItemCustomiseType {

	private String id;
	
	private String foodItemName;
	
	private String itemDescription;
	
	private String customiseType;
	
	private boolean variantsAvailable;
	
	private String variant;
	
	private String extraVariant;
}
