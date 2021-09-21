package com.endeavour.tap4food.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.security.model.User;

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
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId));
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		return foodItems;
	}
	
	public List<FoodStall> getFoodStalls(Long fcId){
		
		Query query = new Query(Criteria.where("foodCourtId").is(fcId));
		
		List<FoodStall> foodStalls = mongoTemplate.find(query, FoodStall.class);
		
		return foodStalls;
	}
}
