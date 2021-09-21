package com.endeavour.tap4food.app.model.offer;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_OFFERS_FOOD_ITEM_CUST_DETAILS)
public class FoodItemCustomizationDetails {

	@Id
	private String id;
	
	private Long offerId;
	
	private String category;
	
	private String subCategory;
	
	private String foodItemId;
	
	private String foodItemName;
	
	private String customiseType;
	
	private Double actualPrice;
	
	private Double offerPrice;
	
	private String offerPercent;
	
	private Long quantity;
}
