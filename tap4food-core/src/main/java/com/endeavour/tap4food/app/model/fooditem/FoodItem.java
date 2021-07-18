package com.endeavour.tap4food.app.model.fooditem;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
	
	@Indexed
	private Long foodItemId;
	
	private String foodItemName;
	
	private String description;
	
	private String category;
	
	private String subCategory;
	
	private String cuisine;
	
	private boolean addOn;
	
	private boolean veg;
	
	private boolean egg;
	
	private boolean isReccommended;
	
	private double price;
	
	private double ratingValue;  
	
	private long totalRatings;
	
	private Long foodStallId;
	
}
