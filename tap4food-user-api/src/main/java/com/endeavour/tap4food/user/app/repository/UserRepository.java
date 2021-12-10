package com.endeavour.tap4food.user.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.user.app.security.model.User;

@Repository
public class UserRepository {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	public Optional<User> findByUserName(String userName){
		Query query = new Query();
		query.addCriteria(Criteria.where("userName").is(userName));
		
		User user = mongoTemplate.findOne(query, User.class);
		
		return Optional.ofNullable(user);
	}
	
	public Optional<User> findByEmailId(String emailId){
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(emailId));
		
		User user = mongoTemplate.findOne(query, User.class);
		
		return Optional.ofNullable(user);
	}
	
	public Optional<User> findByPhoneNumber(String phoneNumber){
		Query query = new Query();
		query.addCriteria(Criteria.where("phoneNumber").is(phoneNumber));
		
		User user = mongoTemplate.findOne(query, User.class);
		
		return Optional.ofNullable(user);
	}
	
	public boolean save(User user) {
		boolean flag = false;
		mongoTemplate.save(user);
		System.out.println("After save: " + user);
		if(user.getId() != null) {
			flag = true;
		}
		return flag;
	}
	
	public boolean updateUniqueNumber(User user) {
		
		Query query = new Query(Criteria.where("phoneNumber").is(user.getPhoneNumber()));
		
		Update update = new Update();
		update.set("uniqueNumber", "");
		
		mongoTemplate.upsert(query, update, Otp.class);
		
		return false;
	}
	
	public List<FoodItem> getFoodItems(Long fsId){
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("baseItem").exists(false)));
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		return foodItems;
	}
	
	public FoodItem getFoodItem(Long foodItemId){
		
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		FoodItem foodItem = mongoTemplate.findOne(query, FoodItem.class);
		
		return foodItem;
	}
	
	public List<FoodItem> getFoodItemCombinations(Long foodItemId){
		
		Query query = new Query(Criteria.where("baseItem").is(foodItemId));
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		return foodItems;
	} 
	
	public List<FoodStall> getFoodStalls(Long fcId){
		
		Query query = new Query(Criteria.where("foodCourtId").is(fcId));
		
		List<FoodStall> foodStalls = mongoTemplate.find(query, FoodStall.class);
		
		return foodStalls;
	}
	
	public FoodCourt getFoodCourt(Long fcId){
		
		Query query = new Query(Criteria.where("foodCourtId").is(fcId));
		
		FoodCourt foodCourt = mongoTemplate.findOne(query, FoodCourt.class);
		
		return foodCourt;
	}
	
	public BusinessUnit getBusinessUnit(Long buId){
		
		Query query = new Query(Criteria.where("businessUnitId").is(buId));
		
		BusinessUnit bu = mongoTemplate.findOne(query, BusinessUnit.class);
		
		return bu;
	}
	
	public FoodItemCustomiseDetails getFoodItemCustomDetails(Long foodItemId) {
		
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		return mongoTemplate.findOne(query, FoodItemCustomiseDetails.class);
	}
	
	public FoodItemPricing getCombinationPrices(Long itemId){
		
		Query query = new Query(Criteria.where("foodItemId").is(itemId));
		
		FoodItemPricing pricingInfo = mongoTemplate.findOne(query, FoodItemPricing.class);
		
		return pricingInfo;
	}
}
