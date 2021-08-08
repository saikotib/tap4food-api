package com.endeavour.tap4food.app.repository;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.Otp;

@Repository
public class CommonRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public void persistOTP(Otp otp) {
		
		System.out.println("In persistOTP() OTP : " + otp);

		Otp existingOtp = getRecentOtp(otp.getPhoneNumber());
		
		System.out.println(existingOtp);
		
		if(Objects.isNull(existingOtp)) {
			mongoTemplate.save(otp);
		}else {
			
			Query query = new Query(Criteria.where("phoneNumber").is(otp.getPhoneNumber()));
			
			Update update = new Update();
			update.set("otp", otp.getOtp());
			update.set("isExpired", false);
			
			mongoTemplate.upsert(query, update, Otp.class);
			
		}
		
	}
	
	public Otp getRecentOtp(final String phoneNumber) {
		
		System.out.println("Phone Number : " + phoneNumber);
		
		Query query = new Query(Criteria.where("phoneNumber").is(phoneNumber));
		
		Otp otp = mongoTemplate.findOne(query, Otp.class, "otp");
		
		return otp;
	}

	public void saveOtp(Otp otp) {
		mongoTemplate.save(otp);
		
	}
	
}
