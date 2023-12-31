package com.endeavour.tap4food.app.model.fooditem;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_FOODITEM_PRICING)
public class FoodItemPricing {

	@Id
	private String id;
	
	private Long foodItemId;
	
	private Long foodStallId;
	
	private String category;
	
	private String subCategory;
	
	private String foodItemName;
	
	private String combination;
	
//	private Double combinationPrice;
	
	private boolean isBaseItem;
		
	private Double price;
	
	private String notes;
	
	private String status;
}
