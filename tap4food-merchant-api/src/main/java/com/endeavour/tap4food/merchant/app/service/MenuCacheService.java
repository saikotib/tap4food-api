package com.endeavour.tap4food.merchant.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.response.dto.FoodItemResponse;
import com.endeavour.tap4food.merchant.app.repository.FoodItemRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MenuCacheService {

	@Autowired
	private FoodItemService foodItemService;
	
	@Autowired
	private FoodItemRepository foodItemRepository;
	
	private Map<Long, List<FoodItemPricing>> itemsPricingMap = new HashMap<Long, List<FoodItemPricing>>();
	
	private Map<Long, List<FoodItemResponse>> foodItemsMap = new HashMap<Long, List<FoodItemResponse>>();
	
	public List<FoodItemPricing> getItemsPricingListFromCache(Long stallId){
		
		if(itemsPricingMap.containsKey(stallId)) {
			return itemsPricingMap.get(stallId);
		}else {
			return Collections.emptyList();
		}
	}
		
	public void addItemsPricingListToCach(Long stallId) {
		
		log.info("Items pricing details are adding to cache for stallId : {}", stallId);
		
//		List<FoodItemPricing> pricingList = foodItemService.getFoodItemPricingDetails(stallId);
		
//		itemsPricingMap.put(stallId, pricingList);
		
		log.info("Items pricing details are added to cache for stallId : {}", stallId);
	}
	
	public List<FoodItemResponse> getFoodItemsListFromCache(Long stallId){
		
		if(foodItemsMap.containsKey(stallId)) {
			return foodItemsMap.get(stallId);
		}else {
			return Collections.emptyList();
		}
	}
	
	public void addFoodItemsListToCache(Long stallId) {
		
		log.info("Items details are adding to cache for stallId : {}", stallId);
		
//		List<FoodItemResponse> itemsList = foodItemService.getFoodItems(stallId);
//		
//		foodItemsMap.put(stallId, itemsList);
		
		log.info("Items details are added to cache for stallId : {}", stallId);
		
//		this.addFoodItemsPricingDetailsToCacheV2(stallId);
	}
	
	public void addFoodItemsPricingDetailsToCacheV3(Long fsId){
		
		long batchCount = 100;
		
		itemsPricingMap.remove(fsId);
		itemsPricingMap.put(fsId, Collections.emptyList());
		
		List<FoodItemPricing> pricingDetails = foodItemRepository.getFoodItemPricingDetails(fsId);
		
		List<FoodItemPricing> latestPricingDetails = new ArrayList<FoodItemPricing>();
		
		for(FoodItemPricing pricing : pricingDetails) {
			
			FoodItem item = null;
			try {
				item = foodItemRepository.getFoodItem(pricing.getFoodItemId());
				
				if(Objects.isNull(item)) {
					continue;
				}
				
//				System.out.println("FoodItem : " + item.getFoodItemName() + " : comb : " + item.getCombination() + " : isDefaultCombination : " + item.isDefaultCombination());
				
				if(item.isDefaultCombination()) {
					continue;
				}
				
				String name = pricing.getFoodItemName();
				name = name.replaceAll("##", " ");
				pricing.setFoodItemName(name);

				pricing.setCombination(item.getCombination());
								
				latestPricingDetails.add(pricing);
				if(latestPricingDetails.size() == batchCount) {
					latestPricingDetails.addAll(itemsPricingMap.get(fsId));
					itemsPricingMap.put(fsId, latestPricingDetails);
				}
				
			} catch (TFException e) {
				e.printStackTrace();
			}
		}
		
		if(!latestPricingDetails.isEmpty()) {
			itemsPricingMap.get(fsId).addAll(latestPricingDetails);
			latestPricingDetails = new ArrayList<FoodItemPricing>();
		}
	}
}
