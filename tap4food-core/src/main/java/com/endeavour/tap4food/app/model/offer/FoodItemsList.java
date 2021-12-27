package com.endeavour.tap4food.app.model.offer;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_OFFERS_FOOD_ITEMS_LISTS)
public class FoodItemsList {
	
	@Id
	private String id;

	private Long foodItemId;
	
	private String itemName;
	
	private Long offerId;
	
	private String listName;
	
	private String description;
	
	private Double actualPrice;
	
	private Double offerPrice;
	
	private boolean customizationFlag;
	
	private Long fsId;
	
	private String selectType;
}
