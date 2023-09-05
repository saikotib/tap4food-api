//package com.endeavour.tap4food.merchant.app.listeners;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
//import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import com.endeavour.tap4food.app.model.fooditem.FoodItem;
//import com.endeavour.tap4food.app.model.fooditem.PreProcessedFoodItems;
//import com.endeavour.tap4food.app.response.dto.FoodItemResponse;
//import com.endeavour.tap4food.merchant.app.repository.FoodItemRepository;
//import com.endeavour.tap4food.merchant.app.repository.PreProcessorRepository;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//public class FoodItemEntityListner extends AbstractMongoEventListener<FoodItem>{
//
//	@Autowired
//	private PreProcessorRepository preProcessorRepository;
//	
//	@Autowired
//	private FoodItemRepository foodItemRepository;
//	
//	@Override
//	public void onAfterSave(AfterSaveEvent<FoodItem> event) {
//		
//		FoodItem newItem = event.getSource();
//		
//		log.info("Data : {}" + event.getDocument());
//		log.info("Data foodItem: {}" + newItem);
//		
//		FoodItemResponse foodItem = new FoodItemResponse();
//		foodItem.setDbId(newItem.getId());
//		foodItem.setAddOn(newItem.isAddOn());
//		foodItem.setCategory(newItem.getCategory());
//		foodItem.setCuisine(newItem.getCuisine());
//		foodItem.setDescription(newItem.getDescription());
//		foodItem.setEgg(newItem.isEgg());
//		foodItem.setFoodItemId(newItem.getFoodItemId());
//		foodItem.setFoodItemName(newItem.getFoodItemName());
//		foodItem.setFoodStallId(newItem.getFoodStallId());
//		foodItem.setPic(newItem.getPic());
//		foodItem.setPrice(Double.valueOf(0));
//		foodItem.setRating(newItem.getRating());
//		foodItem.setReccommended(newItem.isReccommended());
//		foodItem.setSubCategory(newItem.getSubCategory());
//		foodItem.setTotalReviews(newItem.getTotalReviews());
//		foodItem.setVeg(newItem.isVeg());
//		foodItem.setHasCustomizations(newItem.isAvailableCustomisation());
//		foodItem.setStatus(newItem.getStatus());
//		
//		PreProcessedFoodItems preProcessedData = preProcessorRepository.getPreProcessedItems(newItem.getFoodStallId());
//
//		if(Objects.isNull(preProcessedData)) {
//			preProcessedData = new PreProcessedFoodItems();
//			preProcessedData.setFoodStallId(newItem.getFoodStallId());
//			
//			List<FoodItem> foodItems = foodItemRepository.getFoodItems(newItem.getFoodStallId());
//			
//			Map<Long, FoodItemResponse> foodItemsMapById = new HashMap<Long, FoodItemResponse>();
//			Map<String, List<FoodItem>> foodItemsMapByCategory = new HashMap<String, List<FoodItem>>();
//			
//			List<FoodItem> recomendedFoodItems = new ArrayList<FoodItem>();
//			List<FoodItem> vegItems = new ArrayList<FoodItem>();
//			List<FoodItem> eggItems = new ArrayList<FoodItem>();
//			
//			for(FoodItem _item : foodItems) {
//				if(Objects.nonNull(_item.getStatus()) && _item.getStatus().equalsIgnoreCase("DELETED")) {
//					continue;
//				}
//				
//				if(!StringUtils.hasText(_item.getTaxType())) {
//					_item.setTaxType("E");
//				}
//								
//				FoodItemResponse _foodItem = new FoodItemResponse();
//				_foodItem.setDbId(_item.getId());
//				_foodItem.setAddOn(_item.isAddOn());
//				_foodItem.setCategory(_item.getCategory());
//				_foodItem.setCuisine(_item.getCuisine());
//				_foodItem.setDescription(_item.getDescription());
//				_foodItem.setEgg(_item.isEgg());
//				_foodItem.setFoodItemId(_item.getFoodItemId());
//				_foodItem.setFoodItemName(_item.getFoodItemName());
//				_foodItem.setFoodStallId(_item.getFoodStallId());
//				_foodItem.setPic(_item.getPic());
//				_foodItem.setPrice(foodItemRepository.getFoodItemPrice(_item.getFoodItemId()));
//				_foodItem.setRating(_item.getRating());
//				_foodItem.setReccommended(_item.isReccommended());
//				_foodItem.setSubCategory(_item.getSubCategory());
//				_foodItem.setTotalReviews(_item.getTotalReviews());
//				_foodItem.setVeg(_item.isVeg());
//				_foodItem.setHasCustomizations(_item.isAvailableCustomisation());
//				_foodItem.setStatus(_item.getStatus());
//				
//				foodItemsMapById.put(_item.getFoodItemId(), _foodItem);
//				
//				if(!("INACTIVE".equalsIgnoreCase(_item.getStatus()) 
//						|| _item.getPrice() == null 
//						|| _item.getPrice() == 0)) {
//					
//					String category = _item.getCategory();
//					String subcategory = _item.getSubCategory();
//					
//					String categoryAndSubCategory = category;
//					
//					if(!category.equalsIgnoreCase(subcategory)) {
//						categoryAndSubCategory = category + " - " + subcategory;
//					}
//					
//					if(!foodItemsMapByCategory.containsKey(categoryAndSubCategory)) {
//						foodItemsMapByCategory.put(categoryAndSubCategory, new ArrayList<FoodItem>());
//					}
//					
//					foodItemsMapByCategory.get(categoryAndSubCategory).add(_item);
//					
//					if(_item.isReccommended()) {
//						recomendedFoodItems.add(_item);
//					}
//					if(_item.isVeg()) {
//						vegItems.add(_item);
//					}
//					if(_item.isEgg()) {
//						eggItems.add(_item);
//					}
//				}
//				
//			}
//			
//			foodItemsMapByCategory.put("veg", vegItems);
//			foodItemsMapByCategory.put("egg", eggItems);
//			foodItemsMapByCategory.put("Recommended", recomendedFoodItems);
//			
//			preProcessedData.setFoodItemsMapById(foodItemsMapById);
//			preProcessedData.setFoodItemsMapByCategory(foodItemsMapByCategory);
//
//		}else {
//			preProcessedData.getFoodItemsMapById().put(newItem.getFoodItemId(), foodItem);
//			Map<String, List<FoodItem>> foodItemsMapByCategory = preProcessedData.getFoodItemsMapByCategory();
//			String category = newItem.getCategory();
//			String subcategory = newItem.getSubCategory();
//			
//			String categoryAndSubCategory = category;
//			
//			if(!category.equalsIgnoreCase(subcategory)) {
//				categoryAndSubCategory = category + " - " + subcategory;
//			}
//			
//			if(!foodItemsMapByCategory.containsKey(categoryAndSubCategory)) {
//				foodItemsMapByCategory.put(categoryAndSubCategory, new ArrayList<FoodItem>());
//			}
//			
//			foodItemsMapByCategory.get(categoryAndSubCategory).add(newItem);
//			preProcessedData.setFoodItemsMapByCategory(foodItemsMapByCategory);
//		}
//		
//		if(preProcessedData.getFoodStallId() != null) {
//			preProcessorRepository.saveData(preProcessedData);
//			log.info("Data is saved into preProcessedFoodItems collection");
//		}		
//	}
//	
//}
