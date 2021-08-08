package com.endeavour.tap4food.app.model.menu;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_MENU_CUSTOMIZE_FOOD_ITEM)
public class CustFoodItem {

	@Id
	private String id;

	private String customiseType;

	private String foodItemName;

	private Double price;

	private Boolean visible;

	private Long foodStallId;
}
