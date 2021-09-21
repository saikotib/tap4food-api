package com.endeavour.tap4food.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.fooditem.AddOns;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomizationPricing;
import com.endeavour.tap4food.app.model.fooditem.FoodItemDirectOffer;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.repository.FoodItemRepository;
import com.endeavour.tap4food.app.repository.FoodStallRepository;
import com.endeavour.tap4food.app.response.dto.FoodItemResponse;

@Service
public class FoodItemService {
	
	@Autowired
	private FoodItemRepository foodItemRepository;
	
	@Autowired
	private FoodStallRepository foodStallRepository;

	public void addFoodItem(Long merchantId, Long fsId, FoodItem foodItem) throws TFException {
		
		foodItem.setFoodStallId(fsId);
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
				
		if(Objects.isNull(foodStall)) {
			throw new TFException("Foodstall is not found");
		}else {
			FoodItem existingFoodItem = foodItemRepository.getFoodItemByReqId(foodItem.getRequestId());
			
			if(!Objects.isNull(existingFoodItem)) {
				
				foodItem.setId(existingFoodItem.getId());
				foodItem.setPic(existingFoodItem.getPic());
				
				foodItemRepository.addFoodItem(foodItem);
				
				System.out.println("Food Item is added. Now adding to procing");
				this.addItemPricing(foodItem);
			}else {
				System.out.println("creating new item....");
			}
		}
	}
	
	public void addItemPricing(FoodItem foodItem) {
		foodItemRepository.addItemPricing(foodItem);
	}
	
	public FoodItem uploadFoodItemPics(final Long fsId, final String requestId, List<MultipartFile> images) throws TFException {

		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found for the given food stall ID");
		}else {
			
			FoodItem foodItem = foodItemRepository.getFoodItemByReqId(requestId);
			
			List<Binary> existingPics = foodItem.getPic();
			
			if(Objects.isNull(existingPics)) {
				existingPics = new ArrayList<Binary>();
			}
			
			for(MultipartFile inputImage : images) {
				try {
					existingPics.add(new Binary(BsonBinarySubType.BINARY, inputImage.getBytes()));
				} catch (IOException e) {
					throw new TFException(e.getMessage());
				}
			}
			
			foodItem.setPic(existingPics);
			
			foodItemRepository.updateFoodItemPics(foodItem);
			
			System.out.println(">>>" + foodItem);
			
			return foodItem;
		}
	}
	
	public List<FoodItemResponse> getFoodItems(Long fsId){
		
		List<FoodItem> foodItems = foodItemRepository.getFoodItems(fsId);
		
		List<FoodItemResponse> foodItemsResponseList = new ArrayList<FoodItemResponse>();
		
		for(FoodItem item : foodItems) {
			FoodItemResponse foodItem = new FoodItemResponse();
			foodItem.setDbId(item.getId());
			foodItem.setAddOn(item.isAddOn());
			foodItem.setCategory(item.getCategory());
			foodItem.setCuisine(item.getCuisine());
			foodItem.setDescription(item.getDescription());
			foodItem.setEgg(item.isEgg());
			foodItem.setFoodItemId(item.getFoodItemId());
			foodItem.setFoodItemName(item.getFoodItemName());
			foodItem.setFoodStallId(item.getFoodStallId());
			foodItem.setPic(item.getPic());
			foodItem.setPrice(foodItemRepository.getFoodItemPrice(item.getFoodItemId()));
			foodItem.setRating(item.getRating());
			foodItem.setReccommended(item.isReccommended());
			foodItem.setSubCategory(item.getSubCategory());
			foodItem.setTotalReviews(item.getTotalReviews());
			foodItem.setVeg(item.isVeg());
			
			foodItemsResponseList.add(foodItem);
		}
		
		return foodItemsResponseList;
	}
	
	public List<FoodItemPricing> getFoodItemPricingDetails(Long fsId){
		
		return foodItemRepository.getFoodItemPricingDetails(fsId);
	}
	
	public FoodItemPricing updateFoodItemPrice(Long fsId, String pricingId, Double newPrice) throws TFException {
		FoodItemPricing itemPricing = foodItemRepository.updateFoodItemPrice(fsId, pricingId, newPrice);
		
		System.out.println("FoodItem price is updated.");
		
		if(Objects.nonNull(itemPricing)) {
			foodItemRepository.updateFoodItemCustomizingPrice(itemPricing.getFoodItemId(), newPrice);
		}
		
		return itemPricing;
	}
	
	public List<FoodItemCustomizationPricing> getFoodItemCustomizationPricingDetails(Long fsId){
		
		return foodItemRepository.getFoodItemPricingDetailsWithCustomization(fsId);
	}
	
	public List<FoodItemCustomizationPricing> getFoodItemCustomizationPricingDetails(Long fsId, Long foodItemId){
		
		return foodItemRepository.getFoodItemPricingDetailsWithCustomization(fsId, foodItemId);
	}
	
	public List<FoodItemCustomizationPricing> getFoodItemCustomizationPricingDetailsForResponse(Long fsId){
		
		List<FoodItemCustomizationPricing> responseList = new ArrayList<FoodItemCustomizationPricing>();
		
		List<FoodItemCustomizationPricing> existingList = this.getFoodItemCustomizationPricingDetails(fsId);
		
		for(FoodItemCustomizationPricing pricingObject : existingList) {
			pricingObject.setCustomiseType(pricingObject.getCustomiseType().replaceAll("##", " "));
			responseList.add(pricingObject);
		}
		
		
		return responseList;
	}
	
	public FoodItemCustomizationPricing updateFoodItemCustomizationPrice(Long fsId, String pricingId, Double newPrice) {
		System.out.println("In updateFoodItemCustomizationPrice()");
		FoodItemCustomizationPricing itemPricing = foodItemRepository.updateFoodItemCustomizingPrice(fsId, pricingId, newPrice);
		
		return itemPricing;
	}
	
	public List<AddOns> getAddOns(Long fsId){
		
		return foodItemRepository.getAddOns(fsId);
	}
	
	public void addFoodItemCustomiseDetails(String requestId, FoodItemCustomiseDetails foodItemCustomiseDetails) throws TFException {
		
		FoodItem foodItem = foodItemRepository.getFoodItemByReqId(requestId);
		
		foodItemCustomiseDetails.setFoodStallId(foodItem.getFoodStallId());

		foodItemRepository.addFoodItemCustomiseDetails(foodItem.getFoodItemId(), foodItemCustomiseDetails);
		
		if(Objects.nonNull(foodItemCustomiseDetails.getId())) {
			System.out.println("Food item customisation details are saved successfully");
		}
		
		this.addItemCustomizationPricing(foodItem, foodItemCustomiseDetails);
	}
	
	public void addItemCustomizationPricing(FoodItem foodItem, FoodItemCustomiseDetails customizationDetails) {

		String category = foodItem.getCategory();
		String subCategory = foodItem.getSubCategory();
		String foodItemName = foodItem.getFoodItemName();
		Long foodItemId = foodItem.getFoodItemId();
		String foodItemDescription = foodItem.getDescription();
		
		List<String> customiseTypes = customizationDetails.getCustomiseTypes();
		Map<String, List<String>> customizeFoodItems = this.processCustomizationLists(customizationDetails.getCustomiseFoodItems());
		Map<String, List<String>> customiseFoodItemsCustomerSpecifications = this.processCustomizationLists(customizationDetails.getCustomiseFoodItemsCustomerSpecifications());
		Map<String, List<String>> customiseFoodItemsDescriptions = this.processCustomizationLists(customizationDetails.getCustomiseFoodItemsDescriptions());
		Map<String, List<String>> customiseFoodItemsSelectButtons = this.processCustomizationLists(customizationDetails.getCustomiseFoodItemsSelectButtons());
		
		List<String> addOnItems = customizationDetails.getAddOnItemsIds();
		
		boolean isSingleCustType = true;
		
		if(customizeFoodItems.size() > 1) {
			isSingleCustType = false;
		}
		
		List<String> foodItemCombinations = new ArrayList<String>();
		
		System.out.println("customizeFoodItems data for combinations : " + customizeFoodItems);
		
		if(foodItem.isPizza()) {
			int count = 0;
			for(List<String> list : customizeFoodItems.values()) {
				foodItemCombinations = preparePizzaCombinations(foodItemCombinations, list, isSingleCustType, count++);
			}
		}else {
			for(List<String> list : customizeFoodItems.values()) {
				foodItemCombinations = prepareCombinations(foodItemCombinations, list, isSingleCustType);
			}
		}
		
		System.out.println("Combinations : " + foodItemCombinations);
		
		List<FoodItemCustomizationPricing> foodItemCustPricing = new ArrayList<FoodItemCustomizationPricing>();
		List<FoodItemDirectOffer> foodItemOffers = new ArrayList<FoodItemDirectOffer>();
		
		for(String combination : foodItemCombinations) {
			FoodItemCustomizationPricing custPricingData = new FoodItemCustomizationPricing();
			custPricingData.setCategory(category);
			custPricingData.setSubCategory(subCategory);
			custPricingData.setFoodItemId(foodItemId);
			custPricingData.setFoodItemName(foodItemName);
			
			Double foodItemPrice = foodItemRepository.getFoodItemPrice(foodItemId);
			if(Objects.isNull(foodItemPrice))
				custPricingData.setPrice(Double.valueOf(0));
			else
				custPricingData.setPrice(foodItemPrice);
			
			custPricingData.setCustomiseType(combination);
			custPricingData.setFoodStallId(foodItem.getFoodStallId());
			
			foodItemCustPricing.add(custPricingData);
		}
		
		foodItemRepository.addItemCustomizationPricing(foodItemCustPricing);
	}
	
	public List<String> prepareCombinations(List<String> combinations, List<String> list, boolean isSingleCustType) {
		
		List<String> latestCombinations = new ArrayList<String>();
		for(String str1 : combinations) {
			for(String str2 : list) {
				String str = str1 + "##" + str2;
				latestCombinations.add(str);
			}
		}

		if((combinations.isEmpty() && !combinations.containsAll(list)) || isSingleCustType)
			combinations.addAll(list);
		combinations.addAll(latestCombinations);
		
		return combinations;
	}
	
	public List<String> preparePizzaCombinations(List<String> combinations, List<String> list, boolean isSingleCustType, int count) {
		
		List<String> latestCombinations = new ArrayList<String>();
		for(String str1 : combinations) {
			
			String dilimTokens[] = str1.split("##");
			int dilimCount = dilimTokens.length - 1;	
			
			if((count - 1) == dilimCount)
			for(String str2 : list) {
				String str = str1 + "##" + str2;
				latestCombinations.add(str);
			}
		}

		if((combinations.isEmpty() && !combinations.containsAll(list)) || isSingleCustType)
			combinations.addAll(list);
		combinations.addAll(latestCombinations);
		
		return combinations;
	}
	
	private Map<String, List<String>> processCustomizationLists(List<String> dataList){
		Map<String, List<String>> dataMap = new LinkedHashMap<String, List<String>>();
		
		for(String data : dataList) {
			String dataTokens[] = data.split("~");
			
			if(!dataMap.containsKey(dataTokens[0])) {
				dataMap.put(dataTokens[0], new ArrayList<String>());
			}
			
			List<String> processedList = dataMap.get(dataTokens[0]);
			processedList.add(dataTokens[1]);
			dataMap.put(dataTokens[0], processedList);
		}
		
		return dataMap;
	}
}