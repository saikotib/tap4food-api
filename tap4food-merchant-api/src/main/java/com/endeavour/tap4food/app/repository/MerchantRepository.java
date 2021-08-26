package com.endeavour.tap4food.app.repository;

import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.FoodCourtUniqueNumber;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.MerchantBankDetails;
import com.endeavour.tap4food.app.model.UniqueNumber;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.response.dto.StallManager;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Repository
public class MerchantRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
//	@Autowired
//	private FoodStallRepository foodStallRepository;

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
	
	public Merchant getMerchant(Long merchantId) throws TFException {
		
		Optional<Merchant> merchantData = this.findByUniqueNumber(merchantId);
		
		if(merchantData.isPresent()) {
			return merchantData.get();
		}else {
			throw new TFException("Merchant not found");
		}		
	}
	
	public List<StallManager> getStallManagers(Long parentMerchantId) throws TFException {
		
		Query query = new Query(Criteria.where("parentMerchant").is(parentMerchantId));
		
		List<Merchant> merchants = mongoTemplate.find(query, Merchant.class);
		
		List<StallManager> stallManagers = new ArrayList<StallManager>();
		
		for(Merchant manager : merchants) {
			StallManager stallManager = new StallManager();
			stallManager.setEmail(manager.getEmail());
			stallManager.setManagerName(manager.getUserName());
			stallManager.setPhoneNumber(manager.getPhoneNumber());
			stallManager.setManagerId(manager.getUniqueNumber());
			
			query = new Query(Criteria.where("managerId").is(manager.getUniqueNumber()));
			
			FoodStall stall = mongoTemplate.findOne(query, FoodStall.class);

			if(Objects.nonNull(stall)) {
				stallManager.setFoodStallId(stall.getFoodStallId());
				stallManager.setFoodStallName(stall.getFoodStallName());
				
				stallManagers.add(stallManager);
			}
			
		}
		
		return stallManagers;	
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

	public Merchant createMerchant(Merchant merchant) {

		boolean flag = false;
		mongoTemplate.save(merchant);
		if (merchant.getId() != null) {
			flag = true;
		}
		return merchant;
	}

	public boolean updateUniqueNumber(Merchant merchant) {

		// Query query = new
		// Query(Criteria.where("phoneNumber").is(merchant.getPhoneNumber()));

		// Update update = new Update();
		// update.set("uniqueNumber", merchant.getUniqueNumber());

		mongoTemplate.save(merchant);

		return false;
	}

	public Merchant updateMerchant(Merchant merchant, boolean changePasswordFlag) {
		
		if(merchant.getUniqueNumber() != null) {
			Optional<Merchant> existingMerchantData = findByUniqueNumber(merchant.getUniqueNumber());
			if(existingMerchantData.isPresent()) {
				
				Merchant existingMerchant = existingMerchantData.get();
				existingMerchant.setPersonalIdNumber(merchant.getPersonalIdNumber());
				existingMerchant.setUserName(Objects.isNull(merchant.getUserName()) ? existingMerchant.getUserName() : merchant.getUserName());
				existingMerchant.setBlockedTimeMs(merchant.getBlockedTimeMs());
				existingMerchant.setStatus(merchant.getStatus());
				
				if(changePasswordFlag) {
					existingMerchant.setPassword(merchant.getPassword());
				}
				
				mongoTemplate.save(existingMerchant);
			}else {
				merchant = null;
			}
		}else {
			merchant = null;
		}
		
		return merchant;
	}

	public void phoneVerifyStatusUpdate(Merchant merchant) {
		
		mongoTemplate.save(merchant);
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

	public List<BusinessUnit> getBusinessUnits(String country, String state, String city){
		Query query = new Query(Criteria.where("country").is(country).andOperator(Criteria.where("state").is(state), Criteria.where("city").is(city)));
		
		List<BusinessUnit> businessUnits = mongoTemplate.find(query, BusinessUnit.class);
		
		return businessUnits;
	}
	
	public List<FoodCourt> getFoodcourts(Long buId ){
		Query query = new Query(Criteria.where("businessUnitId").is(buId));
		
		List<FoodCourt> foodCourts = mongoTemplate.find(query, FoodCourt.class);
		
		return foodCourts;
	}
}
