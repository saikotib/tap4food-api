package com.endeavour.tap4food.app.model.fooditem;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = MongoCollectionConstant.COLLECTION_FOOD_ITEM)
public class FoodItem {
	
	@Id
	private String id;
	
	private Long foodItemId;
	
	private String foodItemName;
	
	private String description;
	
	private String category;
	
	private String subCategory;
	
	private String cuisine;
	
	private boolean isAddOn;
	
	private boolean isVeg;
	
	private boolean isEgg;
	
	private boolean isReccommended;

	private Binary pic;
	
	private Long foodStallId;
	
}
