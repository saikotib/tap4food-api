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
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomizationPricing;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
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
	
	public FoodItemCustomiseDetails addFoodItemCustomiseDetails(Long foodItemId, FoodItemCustomiseDetails foodItemCustomiseDetails) throws TFException {

		foodItemCustomiseDetails.setFoodItemId(foodItemId);
		
		mongoTemplate.save(foodItemCustomiseDetails);
		
		return foodItemCustomiseDetails;
	}
	
	public void addItemPricing(FoodItem foodItem) {
		
		FoodItemPricing itemPricingInfo = new FoodItemPricing();
		itemPricingInfo.setCategory(foodItem.getCategory());
		itemPricingInfo.setSubCategory(foodItem.getSubCategory());
		itemPricingInfo.setFoodItemName(foodItem.getFoodItemName());
		itemPricingInfo.setPrice(Double.valueOf(0));
		
		mongoTemplate.save(itemPricingInfo);
	}
	
	public void addItemCustomizationRawData(FoodItem foodItem, FoodItemCustomiseDetails customizationDetails) {
		
		FoodItemPricing itemPricingInfo = new FoodItemPricing();
		itemPricingInfo.setCategory(foodItem.getCategory());
		itemPricingInfo.setSubCategory(foodItem.getSubCategory());
		itemPricingInfo.setFoodItemName(foodItem.getFoodItemName());
		itemPricingInfo.setPrice(Double.valueOf(0));
		
		mongoTemplate.save(itemPricingInfo);
	}
	
	public void addItemCustomizationPricing(List<FoodItemCustomizationPricing> pricingDataList) {
		
		for(FoodItemCustomizationPricing pricingData : pricingDataList) {
			mongoTemplate.save(pricingData);
		}
	}
}
