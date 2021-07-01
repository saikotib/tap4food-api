package com.endeavour.tap4food.app.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

@Repository
public class AdminRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	private String adminUsersCollection = MongoCollectionConstant.COLLECTION_ADMIN_USERS;
	
	public Optional<Admin> findByUserName(String userName){
		Query query = new Query();
		query.addCriteria(Criteria.where("userName").is(userName));
		
		Admin admin = mongoTemplate.findOne(query, Admin.class);
		
		return Optional.ofNullable(admin);
	}
	
	public Optional<Admin> findByEmailId(String emailId){
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(emailId));
		
		Admin admin = mongoTemplate.findOne(query, Admin.class);
		
		return Optional.ofNullable(admin);
	}
	
	public Optional<Admin> findByPhoneNumber(String phoneNumber){
		Query query = new Query(Criteria.where("phoneNumber").is(phoneNumber));
		
		Admin admin = mongoTemplate.findOne(query, Admin.class);
		
		return Optional.ofNullable(admin);
	}
	
	public Optional<Admin> findByUniqueNumber(Long uniqueNumber){
		Query query = new Query(Criteria.where("uniqueNumber").is(uniqueNumber));
		
		Admin admin = mongoTemplate.findOne(query, Admin.class);
		
		System.out.println("Merchant : " + admin);
		
		return Optional.ofNullable(admin);
	}
}
