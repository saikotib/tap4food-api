package com.endeavour.tap4food.app.repository;

import static com.mongodb.client.model.Sorts.descending;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.endeavour.tap4food.app.model.FoodCourtUniqueNumber;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.MerchantBankDetails;
import com.endeavour.tap4food.app.model.UniqueNumber;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Repository
public class MerchantRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	private String merchantCollection = MongoCollectionConstant.COLLECTION_MERCHANT_UNIQUE_NUMBER;

	private String foodCourtCollectionName = MongoCollectionConstant.COLLECTION_FOOD_COURT_UNIQUE_NUMBER;

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

		// Query query = new
		// Query(Criteria.where("phoneNumber").is(merchant.getPhoneNumber()));

		// Update update = new Update();
		// update.set("uniqueNumber", merchant.getUniqueNumber());

		mongoTemplate.save(merchant);

		return false;
	}

	public Merchant updateMerchant(Merchant merchant) {
		
		if(merchant.getUniqueNumber() != null) {
			Optional<Merchant> existingMerchantData = findByUniqueNumber(merchant.getUniqueNumber());
			if(existingMerchantData.isPresent()) {
				
				Merchant existingMerchant = existingMerchantData.get();
				existingMerchant.setPersonalIdNumber(merchant.getPersonalIdNumber());
				existingMerchant.setUserName(merchant.getUserName());
				
				mongoTemplate.save(existingMerchant);
			}else {
				merchant = null;
			}
		}else {
			merchant = null;
		}
		
		return merchant;
	}

	

	public Optional<Merchant> findMerchantById(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);
		return Optional.ofNullable(merchant);
	}
	
	public Optional<Merchant> findMerchantByUniqueId(final Long id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("uniqueNumber").is(id));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);
		return Optional.ofNullable(merchant);
	}

	public MerchantBankDetails saveMerchantBankDetails(MerchantBankDetails merchantBankDetails) {

		return mongoTemplate.save(merchantBankDetails);
	}

	public FoodStallTimings savefoodStallTimings(FoodStallTimings foodStallTimings) {
		return mongoTemplate.save(foodStallTimings);
	}

	public Optional<List<FoodStallTimings>> finFoodCourtTimingsByUniqueNumber(@Valid Long uniqueId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("merchantId").is(uniqueId));

		List<FoodStallTimings> foodStallTimingsRes = mongoTemplate.find(query, FoodStallTimings.class);

		return Optional.ofNullable(foodStallTimingsRes);
	}

	public Optional<MerchantBankDetails> findMerchantBankDetailsByUniqueNumber(Long uniqueId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("merchantId").is(uniqueId));

		MerchantBankDetails merchantBankDetailsRes = mongoTemplate.findOne(query, MerchantBankDetails.class);

		return Optional.ofNullable(merchantBankDetailsRes);
	}

	public Collection<WeekDay> saveWeekDay(Set<WeekDay> weekDaysObj) {

		Collection<WeekDay> weekDaysRes = mongoTemplate.insertAll(weekDaysObj);

		return weekDaysRes;
	}

	@Transactional
	public synchronized String getFoodCourtUniqueNumber() {

		String foodCourtUniqueNumber = "T4FFC00000";

		MongoCollection<Document> collection = mongoTemplate.getCollection(foodCourtCollectionName);
		FindIterable<Document> iterdoc = collection.find().sort(descending("foodCourtUniqueNumber"));
		Document document = iterdoc.first();
		String maxVal = null;

		if (collection.countDocuments() == 0) {
			maxVal = "1";
		} else {
			maxVal = String.valueOf(document.get("foodCourtUniqueNumber"));
			maxVal = String.valueOf(Long.parseLong(maxVal) + 1);
		}

		FoodCourtUniqueNumber foodCourtUniqueNumberObj = new FoodCourtUniqueNumber();
		foodCourtUniqueNumberObj.setFoodCourtUniqueNumber(Long.valueOf(maxVal));

		mongoTemplate.save(foodCourtUniqueNumberObj);

		foodCourtUniqueNumber = foodCourtUniqueNumber.substring(0,
				(foodCourtUniqueNumber.length() - (maxVal.length() - 1))) + maxVal;

		/*
		 * if (maxVal.length() == 1) { foodCourtUniqueNumber += maxVal; } else if
		 * (maxVal.length() == 2) { foodCourtUniqueNumber += "00000" + maxVal; } else if
		 * (maxVal.length() == 3) { foodCourtUniqueNumber += "000" + maxVal; } else if
		 * (maxVal.length() == 4) { foodCourtUniqueNumber += "00" + maxVal; } else if
		 * (maxVal.length() == 5) { foodCourtUniqueNumber += "0" + maxVal; } else {
		 * foodCourtUniqueNumber += maxVal; }
		 */

		System.out.println(foodCourtUniqueNumber);
		return foodCourtUniqueNumber;
	}

	public List<WeekDay> findWeekDayByFoodCourtUniqueNumber(String foodStallId) {

		Query query = new Query();
		query.addCriteria(Criteria.where("foodStallId").is(foodStallId));

		List<WeekDay> weekDayRes = mongoTemplate.find(query, WeekDay.class);

		return weekDayRes;
	}

	public void saveOneWeekDay(WeekDay weekDayObj) {
		 mongoTemplate.save(weekDayObj);
		
	}

}
