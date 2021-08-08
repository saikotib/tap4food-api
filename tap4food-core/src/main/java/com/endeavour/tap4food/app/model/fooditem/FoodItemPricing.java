package com.endeavour.tap4food.app.model.fooditem;

import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_FOODITEM_PRICING)
public class FoodItemPricing {

	private String id;
	
	private Long foodItemId;
	
	private String category;
	
	private String subCategory;
	
	private String foodItemName;
		
	private Double price;
}
