package com.endeavour.tap4food.app.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.Otp;

@Repository
public class CommonRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public void persistOTP(Otp otp) {
		
		mongoTemplate.save(otp);
		
	}
	
	public Otp getRecentOtp(final String phoneNumber) {
		
		System.out.println("Phone Number : " + phoneNumber);
		
		Query query = new Query();
		query.addCriteria(Criteria.where("phoneNumber").is(phoneNumber));
		
		Otp otp = mongoTemplate.findOne(query, Otp.class, "otp");
		
		
		return otp;
	}
	
}
