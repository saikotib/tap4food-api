package com.endeavour.tap4food.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.repository.FoodStallRepository;

@Service
public class FoodStallService {
	
	@Autowired
	private FoodStallRepository foodStallRepository;
	
	public FoodStall createFoodStall(Long merchantUniqNumber, FoodStall foodStall) throws TFException {
		
		foodStallRepository.createNewFoodStall(merchantUniqNumber, foodStall);
		
		return foodStall;
	}
}
