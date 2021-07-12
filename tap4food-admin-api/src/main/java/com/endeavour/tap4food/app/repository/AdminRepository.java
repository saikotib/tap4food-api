package com.endeavour.tap4food.app.repository;

import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.UniqueNumber;
import com.endeavour.tap4food.app.model.collection.constants.BusinessUnitCollectionConstants;
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

		Query query = new Query(new Criteria().andOperator(Criteria.where("uniqueNumber").exists(true),
				Criteria.where("uniqueNumber").ne("")));

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

	public BusinessUnit saveBusinessUnit(@Valid BusinessUnit businessUnit) {
		// TODO Auto-generated method stub
		return mongoTemplate.save(businessUnit);
	}

	public boolean deleteBusinessUnitById(String businessUnitId) {
		boolean flag = false;
		mongoTemplate.remove(Query.query(Criteria.where("id").is(businessUnitId)), BusinessUnit.class);
		flag = true;
		return flag;
	}

	public List<BusinessUnit> findBusinessUnitsByFilter(Map<String, Object> filterMap) {

		final Query query = new Query();
		List<BusinessUnit> res = null;
		final List<Criteria> criteria = new ArrayList<>();

		if (filterMap.containsKey(BusinessUnitCollectionConstants.NAME)) {
			System.out.println(BusinessUnitCollectionConstants.NAME);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.NAME)
					.is(filterMap.get(BusinessUnitCollectionConstants.NAME)));
		}
		if (StringUtils.hasText(String.valueOf(filterMap.get(BusinessUnitCollectionConstants.TYPE)))) {
			System.out.println(BusinessUnitCollectionConstants.TYPE);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.TYPE)
					.is(filterMap.get(BusinessUnitCollectionConstants.TYPE)));
		}

		if (StringUtils.hasText(String.valueOf(filterMap.get(BusinessUnitCollectionConstants.CITY)))) {
			System.out.println(BusinessUnitCollectionConstants.CITY);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.CITY)
					.is(filterMap.get(BusinessUnitCollectionConstants.CITY)));
		}

		if (StringUtils.hasText(String.valueOf(filterMap.get(BusinessUnitCollectionConstants.STATUS)))) {
			System.out.println(BusinessUnitCollectionConstants.STATUS);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.STATUS)
					.is(filterMap.get(BusinessUnitCollectionConstants.STATUS)));
		}

		if (StringUtils.hasText(String.valueOf(filterMap.get(BusinessUnitCollectionConstants.STATE)))) {
			System.out.println(BusinessUnitCollectionConstants.STATE);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.STATE)
					.is(filterMap.get(BusinessUnitCollectionConstants.STATE)));
		}

		if (StringUtils.hasText(String.valueOf(filterMap.get(BusinessUnitCollectionConstants.COUNTRY)))) {
			System.out.println(BusinessUnitCollectionConstants.COUNTRY);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.COUNTRY)
					.is(filterMap.get(BusinessUnitCollectionConstants.COUNTRY)));
		}
		if (!criteria.isEmpty()) {
			query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
			res = mongoTemplate.find(query, BusinessUnit.class);
		} else {
			res = mongoTemplate.findAll(BusinessUnit.class);
		}

		System.out.println(query);
		return res;
	}

	public Optional<BusinessUnit> findAdminByBusinessTypeId(String buId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("businessUnitId").is(buId));

		BusinessUnit businessUnit = mongoTemplate.findOne(query, BusinessUnit.class);

		return Optional.ofNullable(businessUnit);
	}

	public FoodCourt saveFoodCourt(FoodCourt foodCourt) {

		return mongoTemplate.save(foodCourt);
	}

	public List<FoodCourt> findFoodCourtsByBusinessTypeId(String buId) {

		return mongoTemplate.findAll(FoodCourt.class);
	}

	public Optional<FoodCourt> findFoodCourtByFoodCourtId(String foodCourtId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("foodCourtId").is(foodCourtId));

		FoodCourt foodCourt = mongoTemplate.findOne(query, FoodCourt.class);
		return Optional.ofNullable(foodCourt);
	}

	public boolean deleteFoodCourtById(final String foodCourtId) {
		boolean flag = false;
		mongoTemplate.remove(Query.query(Criteria.where("foodCourtId").is(foodCourtId)), FoodCourt.class);
		flag = true;
		return flag;
	}
}
