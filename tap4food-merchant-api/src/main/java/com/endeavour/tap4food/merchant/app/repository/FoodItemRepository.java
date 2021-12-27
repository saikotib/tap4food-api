package com.endeavour.tap4food.merchant.app.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.fooditem.AddOns;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomizationPricing;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.model.menu.CustFoodItem;
import com.endeavour.tap4food.app.model.offer.FoodItemCustomizationDetails;
import com.endeavour.tap4food.app.service.CommonSequenceService;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

@Repository
public class FoodItemRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private CommonSequenceService commonSequenceService;
	
	public FoodItem addFoodItem(FoodItem foodItem) throws TFException {
		try {
			foodItem.setFoodItemId(getIdForNewFoodItem());
			mongoTemplate.save(foodItem);
		}catch (Exception e) {
			throw new TFException("Error occured while adding food item");
		}
		return foodItem;
	}
	
	public void updateFoodItem(FoodItem foodItem) throws TFException {
		
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
	
	public FoodItem getFoodItem(Long foodItemId) throws TFException {
		
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		FoodItem foodItem = mongoTemplate.findOne(query, FoodItem.class);
		
		return foodItem;
	}
	
	public FoodItem getChileFoodItem(Long baseItem, String combination) throws TFException {
		
		Query query = new Query(Criteria.where("baseItem").is(baseItem).andOperator(Criteria.where("combination").is(combination)));
		
		FoodItem foodItem = mongoTemplate.findOne(query, FoodItem.class);
		
		return foodItem;
	}
	
	public FoodItemCustomiseDetails getFoodItemCustomizeDetails(Long foodItemId) throws TFException {
		
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		FoodItemCustomiseDetails foodItemCustomiseDetails = mongoTemplate.findOne(query, FoodItemCustomiseDetails.class);
		
		return foodItemCustomiseDetails;
	}
	
	public Map<String, List<String>> getCustomiseFoodItems(Long fsId){
		Query query = new Query(Criteria.where("foodStallId").is(fsId));

		List<CustFoodItem> customiseFoodItems = mongoTemplate.find(query, CustFoodItem.class);
		
		Map<String, List<String>> customiseFoodItemsMap = new HashMap<String, List<String>>();
		
		for(CustFoodItem custFoodItem : customiseFoodItems) {
			if(!customiseFoodItemsMap.containsKey(custFoodItem.getCustomiseType())) {
				customiseFoodItemsMap.put(custFoodItem.getCustomiseType(), new ArrayList<String>());
			}
			
			customiseFoodItemsMap.get(custFoodItem.getCustomiseType()).add(custFoodItem.getFoodItemName());
		}
		
		return customiseFoodItemsMap;
	}
	
	public List<FoodItem> getFoodItems(Long fsId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId)
				.andOperator(Criteria.where("foodItemId").exists(true),
						Criteria.where("baseItem").exists(false),
						Criteria.where("status").ne("DELETED"))
				);
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		return foodItems;
	}
	
	public List<FoodItem> getFoodItemsForOffers(Long fsId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId)
				.andOperator(Criteria.where("foodItemId").exists(true)));
		
		query.fields().exclude("pic");
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		return foodItems;
	}
	
	public List<FoodItem> getCombinationFoodItems(Long fsId, Long foodItemId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId)
				.andOperator(Criteria.where("baseItem").is(foodItemId)));
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		return foodItems;
	}
	
	public Double getFoodItemPrice(Long foodItemId) {
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		FoodItemPricing foodItem = mongoTemplate.findOne(query, FoodItemPricing.class);
		
		return foodItem.getPrice();
	}
	
	public Double getFoodItemPrice(String pricingId) {
		Query query = new Query(Criteria.where("_id").is(pricingId));
		
		FoodItemPricing foodItem = mongoTemplate.findOne(query, FoodItemPricing.class);
		
		return foodItem.getPrice();
	}
	
	public List<FoodItemPricing> getFoodItemPricingDetails(Long fsId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("foodItemId").exists(true),
				Criteria.where("status").ne("DELETED")));
		
		List<FoodItemPricing> foodItems = mongoTemplate.find(query, FoodItemPricing.class);
		
		return foodItems;
	}
	
	public FoodItemPricing getFoodItemPricingDetails(String pricingId){
		
		Query query = new Query(Criteria.where("_id").is(pricingId));
		
		FoodItemPricing foodItem = mongoTemplate.findOne(query, FoodItemPricing.class);
		
		return foodItem;
	}
	
	public FoodItemPricing getFoodItemPricingDetails(Long fsId, Long foodItemId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("foodItemId").is(foodItemId)));
		
		FoodItemPricing foodItem = mongoTemplate.findOne(query, FoodItemPricing.class);
		
		return foodItem;
	}
	
	public FoodItemPricing updateFoodItemPrice(Long fsId, String pricingId, Double itemPrice) {
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("_id").is(pricingId)));
		
		FoodItemPricing itemPricingObject = mongoTemplate.findOne(query, FoodItemPricing.class);
		itemPricingObject.setPrice(itemPrice);
		
		String notes = Objects.isNull(itemPricingObject.getNotes())? "Price update : " + itemPrice : itemPricingObject.getNotes() + " ## " + "New Price updated :" + itemPrice;
		
		itemPricingObject.setNotes(notes);
		
		mongoTemplate.save(itemPricingObject);
		
		return itemPricingObject;
	}
	
	public FoodItemPricing updateCombinationFoodItemPrice(Long foodItemId, Double itemPrice) {
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		FoodItemPricing itemPricingObject = mongoTemplate.findOne(query, FoodItemPricing.class);
		itemPricingObject.setPrice(itemPrice + itemPricingObject.getPrice());
		
		String notes = Objects.isNull(itemPricingObject.getNotes())? "Price update : " + itemPricingObject.getPrice() : itemPricingObject.getNotes() + " ## " + "New Price updated :" + itemPricingObject.getPrice();
		
		itemPricingObject.setNotes(notes);
		
		mongoTemplate.save(itemPricingObject);
		
		return itemPricingObject;
	}
	
	public List<FoodItemCustomizationPricing> getFoodItemPricingDetailsWithCustomization(Long fsId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("foodItemId").exists(true)));
		
		List<FoodItemCustomizationPricing> foodItems = mongoTemplate.find(query, FoodItemCustomizationPricing.class);
		
		return foodItems;
	}
	
	public List<FoodItemCustomizationPricing> getFoodItemPricingDetailsWithCustomization(Long fsId, Long foodItemId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("foodItemId").is(foodItemId)));
		
		List<FoodItemCustomizationPricing> foodItems = mongoTemplate.find(query, FoodItemCustomizationPricing.class);
		
		return foodItems;
	}
	
	public FoodItemCustomizationPricing updateFoodItemCustomizingPrice(Long fsId, String pricingId, Double newPrice) {
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("_id").is(pricingId)));
		
		FoodItemCustomizationPricing itemPricingObject = mongoTemplate.findOne(query, FoodItemCustomizationPricing.class);
		
//		System.out.println(">>" + itemPricingObject);
		itemPricingObject.setPrice(newPrice);
		
		String notes = Objects.isNull(itemPricingObject.getNotes())? "Price update : " + newPrice : itemPricingObject.getNotes() + " ## " + "New Price updated :" + newPrice;
		
		itemPricingObject.setNotes(notes);
		
		mongoTemplate.save(itemPricingObject);
		
		return itemPricingObject;
	}
	
	
	
	public void updateFoodItemCustomizingPrice(Long foodItemId, Double newPrice) {
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		Update update = new Update();
		update.set("price", newPrice);
		
		mongoTemplate.updateMulti(query, update, FoodItemCustomizationPricing.class);
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
//		itemPricingInfo.setCombinationPrice(Double.valueOf(0));
		
		if(StringUtils.hasText(foodItem.getCombination())) {
			itemPricingInfo.setCombination(foodItem.getCombination());
			itemPricingInfo.setBaseItem(false);
		}else {
			itemPricingInfo.setBaseItem(true);
		}
		
		itemPricingInfo.setFoodItemId(foodItem.getFoodItemId());
		itemPricingInfo.setFoodStallId(foodItem.getFoodStallId());
		
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
	
	public void deleteFoodItem(Long foodItemId) {
		
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		FoodItem foodItem = mongoTemplate.findOne(query, FoodItem.class);
		
		foodItem.setStatus("DELETED");
		
		mongoTemplate.save(foodItem);
		
		FoodItemPricing foodItemPricing = mongoTemplate.findOne(query, FoodItemPricing.class);
		
		foodItemPricing.setStatus("DELETED");
		
		mongoTemplate.save(foodItemPricing);
	}
}
