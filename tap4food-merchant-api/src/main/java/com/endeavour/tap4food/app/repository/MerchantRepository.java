package com.endeavour.tap4food.app.repository;

import static com.mongodb.client.model.Sorts.descending;

import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.UniqueNumber;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Repository
public class MerchantRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	private String merchantCollection = MongoCollectionConstant.COLLECTION_MERCHANT_UNIQUE_NUMBER;

	public Optional<Merchant> findByUserName(String userName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("userName").is(userName));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);

		return Optional.ofNullable(merchant);
	}

	public Optional<Merchant> findByEmailId(String emailId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(emailId));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);

		return Optional.ofNullable(merchant);
	}

	public Optional<Merchant> findByPhoneNumber(String phoneNumber) {
		Query query = new Query(Criteria.where("phoneNumber").is(phoneNumber));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);

		return Optional.ofNullable(merchant);
	}

	public Optional<Merchant> findByUniqueNumber(Long uniqueNumber) {
		Query query = new Query(Criteria.where("uniqueNumber").is(uniqueNumber));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);

		System.out.println("Merchant : " + merchant);

		return Optional.ofNullable(merchant);
	}

	public boolean save(Merchant merchant) {
		boolean flag = false;
		mongoTemplate.save(merchant);
		System.out.println("After save: " + merchant);
		if (merchant.getId() != null) {
			flag = true;
		}
		return flag;
	}
	
	
	
	@Transactional
	public synchronized Long getRecentUniqueNumber() {
		
		 MongoCollection<Document> collection = mongoTemplate.getCollection(merchantCollection);

		 long numberOdDocuments = collection.countDocuments();
		 
		 String maxval = null;
		 
		 if(numberOdDocuments == 0) {
			 maxval = "1010";
		 }else {
			 Bson sort = descending("uniqueNumber");
			 
			 FindIterable<Document> iterdoc = collection.find().sort(sort);
			
			 Document document = iterdoc.first();
			 
			 maxval = String.valueOf(document.get("uniqueNumber"));
		 }
		 
		 Long nextMaxVal = Long.valueOf(maxval) + 1;
		 
		 System.out.println("Max Value : " + maxval);
		 
		 UniqueNumber uniqueNumber = new UniqueNumber();
		 uniqueNumber.setUniqueNumber(nextMaxVal);
		 
		 mongoTemplate.save(uniqueNumber);
		 
		return nextMaxVal;
	}
	
	public boolean createPassword(final Long uniqueNumber, final String password) {
		
		boolean flag = false;
		
		Query query = new Query(Criteria.where("uniqueNumber").is(uniqueNumber));
		
		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);
		
		merchant.setPassword(password);
		
		mongoTemplate.save(merchant);
		
		flag = true;

		return flag;
	}
	
	public Optional<Merchant> findByMerchantByPhoneNumber(String phoneNumber) {
		Query query = new Query(Criteria.where("phoneNumber").is(phoneNumber));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);

		return Optional.ofNullable(merchant);
	}
	
	
	public boolean createMerchant(Merchant merchant) {

		boolean flag = false;
		mongoTemplate.save(merchant);
		if (merchant.getId() != null) {
			flag = true;
		}
		return flag;
	}

	public boolean updateUniqueNumber(Merchant merchant) {

//		Query query = new Query(Criteria.where("phoneNumber").is(merchant.getPhoneNumber()));

//		Update update = new Update();
//		update.set("uniqueNumber", merchant.getUniqueNumber());

		mongoTemplate.save(merchant);

		return false;
	}

	public Optional<Merchant> saveMerchant(Merchant merchant) {
		Merchant merchantRes = mongoTemplate.save(merchant);
		System.out.println(merchantRes);
		return Optional.ofNullable(merchantRes);
	}
}
