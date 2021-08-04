package com.endeavour.tap4food.app.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.fooditem.AddOns;
import com.endeavour.tap4food.app.model.fooditem.CustomisedFoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.service.CommonSequenceService;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

@Repository
public class FoodItemRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private CommonSequenceService commonSequenceService;
	
	public void addFoodItem(FoodItem foodItem) throws TFException {
		try {
			foodItem.setFoodItemId(getIdForNewFoodItem());
			mongoTemplate.save(foodItem);
		}catch (Exception e) {
			throw new TFException("Error occured while adding food item");
		}
	}
	
	public void updateFoodItemPics(FoodItem foodItem) throws TFException {
		
		mongoTemplate.save(foodItem);
		
	}
	
	public void deleteDummy(FoodItem foodItem) throws TFException {
		
		Query query = new Query(Criteria.where("id").is(foodItem.getId()));
		
		mongoTemplate.remove(query, FoodItem.class);
		
	}
	
	public FoodItem getFoodItemByReqId(String requestId) throws TFException {
		
		Query query = new Query(Criteria.where("requestId").is(requestId));
		
		FoodItem foodItem = mongoTemplate.findOne(query, FoodItem.class);
		
		if(Objects.isNull(foodItem)) {
			
			foodItem = new FoodItem();
			foodItem.setRequestId(requestId);
			mongoTemplate.save(foodItem);
		}
		
		return foodItem;
	}
	
	public List<FoodItem> getFoodItems(Long fsId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("foodItemId").exists(true)));
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		return foodItems;
	}
	
	public List<AddOns> getAddOns(Long fsId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("isAddOn").is(true)));
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		List<AddOns> addOns = new ArrayList<AddOns>();
		
		for(FoodItem foodItem : foodItems) {
			AddOns addOn = new AddOns();
			addOn.setFoodItemId(foodItem.getFoodItemId());
			addOn.setItemName(foodItem.getFoodItemName());
			
			addOns.add(addOn);
		}
		
		return addOns;
	}
	
	private Long getIdForNewFoodItem() {

		Long foodStallID = commonSequenceService
				.getFoodStallNextSequence(MongoCollectionConstant.COLLECTION_FOODSTALL_SEQ);

		return foodStallID;
	}
	
	public void addCustomisedFoodItems(Long foodItemId, List<CustomisedFoodItem> customisedFoodItems) throws TFException {

		
		for(CustomisedFoodItem customisedFoodItem : customisedFoodItems) {
			
			customisedFoodItem.setFoodItemId(foodItemId);
			mongoTemplate.save(customisedFoodItem);
		}
		
	}
	
	public List<CustomisedFoodItem> getCustomisedFoodItems(Long foodItemId){
		
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		List<CustomisedFoodItem> customisedFoodItems = mongoTemplate.find(query, CustomisedFoodItem.class);
		
		return customisedFoodItems;
	}
}
