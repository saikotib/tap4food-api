package com.endeavour.tap4food.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.fooditem.AddOns;
import com.endeavour.tap4food.app.model.fooditem.CustomisedFoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.repository.FoodItemRepository;
import com.endeavour.tap4food.app.repository.FoodStallRepository;

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
			foodItemRepository.addFoodItem(foodItem);
		}
	}
	
	public void uploadFoodItemPics(final Long fsId, final String requestId, List<MultipartFile> images) throws TFException {

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
			
			foodStallRepository.updateFoodStallPic(foodStall);
		}
	}
	
	public List<FoodItem> getFoodItems(Long fsId){
		
		return foodItemRepository.getFoodItems(fsId);
	}
	
	public List<AddOns> getAddOns(Long fsId){
		
		return foodItemRepository.getAddOns(fsId);
	}
	
	public void addCustomisedFoodItems(String requestId, List<CustomisedFoodItem> customisedFoodItems) throws TFException {
		
		FoodItem foodItem = foodItemRepository.getFoodItemByReqId(requestId);

		foodItemRepository.addCustomisedFoodItems(foodItem.getFoodItemId(), customisedFoodItems);
	}
	
	public List<CustomisedFoodItem> getCustomisedFoodItems(Long foodItemId){
		
		return foodItemRepository.getCustomisedFoodItems(foodItemId);
	}
	
}
