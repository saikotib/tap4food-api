package com.endeavour.tap4food.app.model.fooditem;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.response.dto.FoodItemResponse;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_PRE_PROCESSED_FOODITEMS)
public class PreProcessedFoodItems {

	@Id
	private String id;
	
	private Long foodStallId;
	
	private Map<Long, FoodItemResponse> foodItemsMapById;
	
	private Map<String, List<FoodItem>> foodItemsMapByCategory;
}
