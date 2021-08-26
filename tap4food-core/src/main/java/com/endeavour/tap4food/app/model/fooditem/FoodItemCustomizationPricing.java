package com.endeavour.tap4food.app.model.fooditem;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_FOODITEM_CUST_PRICING)
public class FoodItemCustomizationPricing {

	@Id
	private String id;
	
	private Long foodItemId;
	
	private Long foodStallId;
	
	private String category;
	
	private String subCategory;
	
	private String foodItemName;
	
	private String customiseType;
	
	private Double price;
	
	private String notes;
	
}
