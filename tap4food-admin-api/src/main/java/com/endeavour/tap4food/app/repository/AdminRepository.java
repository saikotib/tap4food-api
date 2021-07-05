package com.endeavour.tap4food.app.repository;

import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.UniqueNumber;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Repository
public class AdminRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	private String adminUsersCollection = MongoCollectionConstant.COLLECTION_ADMIN_USERS;

	private String merchantCollection = MongoCollectionConstant.COLLECTION_MERCHANT_UNIQUE_NUMBER;

	public Optional<Admin> findAdminByUserName(String userName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("userName").is(userName));

		Admin admin = mongoTemplate.findOne(query, Admin.class);

		return Optional.ofNullable(admin);
	}

	public Optional<Admin> findAdminByEmailId(String emailId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(emailId));

		Admin admin = mongoTemplate.findOne(query, Admin.class);

		return Optional.ofNullable(admin);
	}

	public Optional<Admin> findAdminByPhoneNumber(String phoneNumber) {
		Query query = new Query(Criteria.where("phoneNumber").is(phoneNumber));

		Admin admin = mongoTemplate.findOne(query, Admin.class);

		return Optional.ofNullable(admin);
	}

	public Optional<Admin> findByUniqueNumber(Long uniqueNumber) {
		Query query = new Query(Criteria.where("uniqueNumber").is(uniqueNumber));

		Admin admin = mongoTemplate.findOne(query, Admin.class);

		System.out.println("Merchant : " + admin);

		return Optional.ofNullable(admin);
	}

	public Optional<Merchant> findMerchantByPhoneNumber(String phoneNumber) {
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

	public Optional<Merchant> findMerchantByEmail(final String merchantEmail) {
		Query query = new Query(Criteria.where("email").is(merchantEmail));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);

		return Optional.ofNullable(merchant);
	}

	@Transactional
	public synchronized Long getRecentUniqueNumber() {

		MongoCollection<Document> collection = mongoTemplate.getCollection(merchantCollection);

		long numberOdDocuments = collection.countDocuments();

		String maxval = null;

		if (numberOdDocuments == 0) {
			maxval = "1010";
		} else {
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

	public boolean updateUniqueNumber(Merchant merchant) {

		mongoTemplate.save(merchant);

		return false;
	}

	public List<Merchant> fetchMerchants() {
		
		Query query = new Query(
				new Criteria().andOperator(
				        Criteria.where("uniqueNumber").exists(true),
				        Criteria.where("uniqueNumber").ne("")
				    ));

		List<Merchant> merchants = mongoTemplate.find(query, Merchant.class);

		return merchants;
	}

	public boolean createAdminPassword(final String userName, final String password) {

		boolean flag = false;

		Query query = new Query(Criteria.where("userName").is(userName));

		Admin admin = mongoTemplate.findOne(query, Admin.class);

		admin.setPassword(password);

		mongoTemplate.save(admin);

		flag = true;

		return flag;
	}
}
