package com.endeavour.tap4food.merchant.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.fooditem.AddOns;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomizationPricing;
import com.endeavour.tap4food.app.model.fooditem.FoodItemDirectOffer;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.response.dto.FoodItemResponse;
import com.endeavour.tap4food.merchant.app.repository.FoodItemRepository;
import com.endeavour.tap4food.merchant.app.repository.FoodStallRepository;

@Service
public class FoodItemService {
	
	@Autowired
	private FoodItemRepository foodItemRepository;
	
	@Autowired
	private FoodStallRepository foodStallRepository;

	public void addFoodItem(Long merchantId, Long fsId, FoodItem foodItem) throws TFException {
		
		foodItem.setFoodStallId(fsId);
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		System.out.println("FoodStall : " + foodStall);
				
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
			
			foodItemRepository.updateFoodItem(foodItem);
			
			System.out.println(">>>" + foodItem);
			
			return foodItem;
		}
	}
	
	public List<FoodItemResponse> getFoodItems(Long fsId){
		
		List<FoodItem> foodItems = foodItemRepository.getFoodItems(fsId);
		
		List<FoodItemResponse> foodItemsResponseList = new ArrayList<FoodItemResponse>();
		
		for(FoodItem item : foodItems) {
			if(Objects.nonNull(item.getStatus()) && item.getStatus().equalsIgnoreCase("DELETED")) {
				continue;
			}
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
			foodItem.setHasCustomizations(item.isAvailableCustomisation());
			
			foodItemsResponseList.add(foodItem);
		}
		
		return foodItemsResponseList;
	}
	
	public List<FoodItemResponse> getFoodItemsForOffers(Long fsId){
		
		List<FoodItem> foodItems = foodItemRepository.getFoodItemsForOffers(fsId);
		
		List<FoodItemResponse> foodItemsResponseList = new ArrayList<FoodItemResponse>();
		
		for(FoodItem item : foodItems) {
			
			if(item.isDefaultCombination()) {
				continue;
			}
			
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
			foodItem.setPrice(foodItemRepository.getFoodItemPrice(item.getFoodItemId()));
			foodItem.setRating(item.getRating());
			foodItem.setReccommended(item.isReccommended());
			foodItem.setSubCategory(item.getSubCategory());
			foodItem.setTotalReviews(item.getTotalReviews());
			foodItem.setVeg(item.isVeg());
			foodItem.setCombination(StringUtils.isEmpty(item.getCombination()) ? "" : item.getCombination().replaceAll("##", " "));
			
			foodItemsResponseList.add(foodItem);
		}
		
		return foodItemsResponseList;
	}
	
	public List<FoodItemPricing> getFoodItemPricingDetails(Long fsId){
		
		List<FoodItemPricing> pricingDetails = foodItemRepository.getFoodItemPricingDetails(fsId);
		
		List<FoodItemPricing> latestPricingDetails = new ArrayList<FoodItemPricing>();
		
		for(FoodItemPricing pricing : pricingDetails) {
			
			System.out.println("Pricing : " + pricing.getFoodItemName());
			
			FoodItem item = null;
			try {
				item = foodItemRepository.getFoodItem(pricing.getFoodItemId());
				
				System.out.println("FoodItem : " + item.getFoodItemName() + " : comb : " + item.getCombination() + " : isDefaultCombination : " + item.isDefaultCombination());
				
				if(item.isDefaultCombination()) {
					continue;
				}
				
				String name = pricing.getFoodItemName();
				name = name.replaceAll("##", " ");
				pricing.setFoodItemName(name);

				pricing.setCombination(item.getCombination());
				
				latestPricingDetails.add(pricing);
				
			} catch (TFException e) {
				e.printStackTrace();
			}
			
			
		}
		
		
		return latestPricingDetails;
	}
	
	public List<FoodItem> getCombinationFoodItems(Long fsId, Long baseItemId){
		
		return foodItemRepository.getCombinationFoodItems(fsId, baseItemId);
	}
	
	public FoodItemPricing updateFoodItemPrice(Long fsId, String pricingId, Double newPrice) throws TFException {
		
		FoodItemPricing itemPricingExistingDetails = foodItemRepository.getFoodItemPricingDetails(pricingId);
		
		Double foodItemExistingPrice = itemPricingExistingDetails.getPrice();
		
		FoodItem foodItem = foodItemRepository.getFoodItem(itemPricingExistingDetails.getFoodItemId());
		
		foodItem.setPrice(newPrice);
		
		if(Objects.isNull(foodItem.getBaseItem())) {
			FoodItem childFoodItem = foodItemRepository.getChileFoodItem(foodItem.getFoodItemId(), foodItem.getCombination());
			
			System.out.println("childFoodItem id : " + childFoodItem);
			
			if(Objects.nonNull(childFoodItem)) {
				childFoodItem.setPrice(newPrice);
				foodItemRepository.updateFoodItem(childFoodItem); 
				
				FoodItemPricing childItemPricingInfo = foodItemRepository.getFoodItemPricingDetails(fsId, childFoodItem.getFoodItemId());
				
				foodItemRepository.updateFoodItemPrice(fsId, childItemPricingInfo.getId(), newPrice);
			}
		}
		
		foodItemRepository.updateFoodItem(foodItem);   // Just to update the latest price of food item
		
		FoodItemPricing itemPricing = foodItemRepository.updateFoodItemPrice(fsId, pricingId, newPrice);
		
		System.out.println("FoodItem price is updated.");
		
		System.out.println("CustType comb name : " + foodItem.getFoodItemId() + " : " + foodItem.getCombination());
		
		Long foodItemId = Objects.isNull(foodItem.getBaseItem()) ? foodItem.getFoodItemId() : foodItem.getBaseItem();
		
		List<FoodItemCustomizationPricing> foodItemCustomizationPricingDetails = this.getFoodItemCustomizationPricingDetails(fsId, foodItemId);
		
		for(FoodItemCustomizationPricing foodItemCustomizationPricing : foodItemCustomizationPricingDetails) {
			
			Double existingPrice = foodItemCustomizationPricing.getPrice();
			
			String combination = foodItemCustomizationPricing.getCustomiseType();
			List<String> combinationTokens = Arrays.asList(combination.split("##"));
			
				String custNameTokens[] = foodItem.getCombination().split("##");
				
				boolean flag = true;
				for(int i = 0; i < custNameTokens.length; i++) {
					if(!combinationTokens.contains(custNameTokens[i])) {
						flag = false;
						break;
					}
				}
				
				if(!flag) {
					continue;
				}
				
				if(existingPrice == 0) {
					System.out.println("In true case");
					foodItemCustomizationPricing.setPrice(newPrice);
				}else {
					System.out.println("In false case");
					System.out.println("In false case existingPrice : " + existingPrice);
					System.out.println("In false case foodItemExistingPrice : " + foodItemExistingPrice);
					existingPrice = existingPrice - foodItemExistingPrice;
					Double revisedPrice = existingPrice + newPrice;
					
					System.out.println("In false case revisedPrice : " + revisedPrice);
					
					foodItemCustomizationPricing.setPrice(revisedPrice);
				}
				
				foodItemRepository.updateFoodItemCustomizingPrice(fsId, foodItemCustomizationPricing.getId(), foodItemCustomizationPricing.getPrice());
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
		
		foodItem.setAvailableCustomisation(true);
		
		foodItemRepository.updateFoodItem(foodItem);
		
		foodItemCustomiseDetails.setFoodStallId(foodItem.getFoodStallId());

//		foodItem.setPizza(true);
		foodItemRepository.addFoodItemCustomiseDetails(foodItem.getFoodItemId(), foodItemCustomiseDetails);
		
		if(Objects.nonNull(foodItemCustomiseDetails.getId())) {
			System.out.println("Food item customisation details are saved successfully");
		}
		
		this.addItemCustomizationPricing(foodItem, foodItemCustomiseDetails);
	}
	
	public void addItemCustomizationPricing(FoodItem foodItem, FoodItemCustomiseDetails customizationDetails) throws TFException {

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
		
		List<String> foodItemCustCombinations = new ArrayList<String>();
		
		System.out.println("customizeFoodItems data for combinations : " + customizeFoodItems);
		
		if(foodItem.isPizza()) {
			int count = 0;
			for(List<String> list : customizeFoodItems.values()) {
				foodItemCustCombinations = preparePizzaCombinations(foodItemCustCombinations, list, isSingleCustType, count++);
				foodItemCombinations = preparePizzaCombinations(foodItemCombinations, list, isSingleCustType);
			}
			
		}else {
			for(List<String> list : customizeFoodItems.values()) {
				foodItemCustCombinations = prepareCombinations(foodItemCustCombinations, list, isSingleCustType);
			}
		}
		
		System.out.println("foodItemCombinations >>" + foodItemCombinations);
		
		if(foodItem.isPizza()) {
			boolean isDefaultCombination = true;
			for(String combination : foodItemCombinations) {
				
				if(!combination.contains("##") || (combination.indexOf("##") < combination.lastIndexOf("##"))) {
					continue;
				}
				
				FoodItem custSupportItem = new FoodItem();
				
				custSupportItem.setAddOn(false);
				custSupportItem.setBaseItem(foodItem.getFoodItemId());
				custSupportItem.setFoodStallId(foodItem.getFoodStallId());
				
				custSupportItem.setCombination(combination);
				
				custSupportItem.setFoodItemName(foodItem.getFoodItemName());
				
				custSupportItem.setCategory(foodItem.getCategory());
				custSupportItem.setSubCategory(foodItem.getSubCategory());
				custSupportItem.setAvailableCustomisation(false);
				custSupportItem.setDescription("NA");
				custSupportItem.setCuisine(foodItem.getCuisine());
				custSupportItem.setPizza(foodItem.isPizza());
				custSupportItem.setDefaultCombination(isDefaultCombination);
				
				if(isDefaultCombination && foodItem.isPizza()) {
					foodItem.setCombination(combination);
					foodItemRepository.updateFoodItem(foodItem);
					System.out.println("Base Item updated with combination : " + foodItem.getCombination() + " : " + foodItem.getFoodItemName());
				}
				
				custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
				this.addItemPricing(custSupportItem);
				
				isDefaultCombination = false;
			}
		}else {
			boolean isDefaultCombination = true;
			for(String combination : foodItemCustCombinations) {
				FoodItem custSupportItem = new FoodItem();
				
				custSupportItem.setAddOn(false);
				custSupportItem.setBaseItem(foodItem.getFoodItemId());
				custSupportItem.setFoodStallId(foodItem.getFoodStallId());
				
				custSupportItem.setCombination(combination);
				
				custSupportItem.setFoodItemName(foodItem.getFoodItemName());
				custSupportItem.setCategory(foodItem.getCategory());
				custSupportItem.setSubCategory(foodItem.getSubCategory());
				custSupportItem.setAvailableCustomisation(false);
				custSupportItem.setDescription("NA");
				custSupportItem.setCuisine(foodItem.getCuisine());
				custSupportItem.setPizza(foodItem.isPizza());
				custSupportItem.setDefaultCombination(isDefaultCombination);
				
				if(isDefaultCombination) {
					foodItem.setCombination(combination);
					foodItemRepository.updateFoodItem(foodItem);
					System.out.println("Base Item updated with combination : " + foodItem.getCombination() + " : " + foodItem.getFoodItemName());
				}
				
				custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
				this.addItemPricing(custSupportItem);
				
				isDefaultCombination = false;
			}

			/* for(List<String> list : customizeFoodItems.values()) {
				for(String custName : list) {
					FoodItem custSupportItem = new FoodItem();
					
					custSupportItem.setAddOn(false);
					custSupportItem.setBaseItem(foodItem.getFoodItemId());
					custSupportItem.setFoodStallId(foodItem.getFoodStallId());
					
					custSupportItem.setCombination(custName);
					
					custSupportItem.setFoodItemName(foodItem.getFoodItemName());
					custSupportItem.setCategory(foodItem.getCategory());
					custSupportItem.setSubCategory(foodItem.getSubCategory());
					custSupportItem.setAvailableCustomisation(false);
					custSupportItem.setDescription("NA");
					custSupportItem.setCuisine(foodItem.getCuisine());
					custSupportItem.setPizza(foodItem.isPizza());
					
					custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
					this.addItemPricing(custSupportItem);
				
				}
			} */
		}
		
		System.out.println("Combinations : " + foodItemCustCombinations);
		
		List<FoodItemCustomizationPricing> foodItemCustPricing = new ArrayList<FoodItemCustomizationPricing>();
		List<FoodItemDirectOffer> foodItemOffers = new ArrayList<FoodItemDirectOffer>();
		
		for(String combination : foodItemCustCombinations) {
			if(foodItem.isPizza()) {
				
				if(!combination.contains("##") || combination.indexOf("##") == combination.lastIndexOf("##"))
				continue;
				//This is to skip the single items for PIZZA
			}
			
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
	
	public List<String> preparePizzaCombinations(List<String> combinations, List<String> list, boolean isSingleCustType) {
		
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