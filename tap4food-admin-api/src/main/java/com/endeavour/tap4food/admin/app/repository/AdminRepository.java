package com.endeavour.tap4food.admin.app.repository;

import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.AdminRole;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.RoleConfiguration;
import com.endeavour.tap4food.app.model.Subscription;
import com.endeavour.tap4food.app.model.UniqueNumber;
import com.endeavour.tap4food.app.model.admin.AboutUs;
import com.endeavour.tap4food.app.model.admin.TermsNConditions;
import com.endeavour.tap4food.app.model.collection.constants.BusinessUnitCollectionConstants;
import com.endeavour.tap4food.app.model.collection.constants.FoodCourtCollectionConstants;
import com.endeavour.tap4food.app.model.collection.constants.FoodStallCollectionConstants;
import com.endeavour.tap4food.app.service.CommonSequenceService;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Repository
public class AdminRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private CommonSequenceService commonSequenceService;

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

	public Optional<Merchant> findMerchantByUniqueNumber(Long uniqueNumber) {
		Query query = new Query(Criteria.where("uniqueNumber").is(uniqueNumber));

		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);

		return Optional.ofNullable(merchant);
	}
	
	public FoodStall getFoodStall(Long foodstallId) {
		Query query = new Query(Criteria.where("foodStallId").is(foodstallId));

		FoodStall foodstall = mongoTemplate.findOne(query, FoodStall.class);

		return foodstall;
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

	public Map<Long, Merchant> fetchMerchants() {

		Query query = new Query(new Criteria().andOperator(Criteria.where("uniqueNumber").exists(true),
				Criteria.where("uniqueNumber").ne(""),  
				Criteria.where("isPhoneNumberVerified").is(true)));

//		query.fields().include("uniqueNumber", "userName", "phoneNumber", "email", "personalIdNumber", "createdBy", "status", "createdDate", "phoneNumberVerified", "profilePic");
		List<Merchant> merchants = mongoTemplate.find(query, Merchant.class);
		
		Map<Long, Merchant> merchantMap = new HashMap<Long, Merchant>();
		
		for(Merchant merchant : merchants) {
			merchantMap.put(merchant.getUniqueNumber(), merchant);
		}

		return merchantMap;
	}
	
	public Map<Long, List<FoodStall>> getFoodStalls(){
		Query query = new Query();
		
//		query.fields().include("foodStallId", "foodStallName", "foodStallLicenseNumber", "merchantId", 
//				"location", "foodCourtName", "buType", "buName", "deliveryTime", "country", "state", "city", "createdDate");

		List<FoodStall> foodStalls = mongoTemplate.find(query, FoodStall.class);
		
		Map<Long, List<FoodStall>> foodStallMap = new HashMap<Long, List<FoodStall>>();
		
		for(FoodStall stall : foodStalls) {
			if(!foodStallMap.containsKey(stall.getMerchantId())) {
				foodStallMap.put(stall.getMerchantId(), new ArrayList<FoodStall>());
			}
			
			foodStallMap.get(stall.getMerchantId()).add(stall);
		}
		
		return foodStallMap;
	}
	
	public FoodStall updateMerchantStatus(Long uniqueNumber, Long foodstallId, String status) throws TFException {

		FoodStall foodstall = this.getFoodStall(foodstallId);

		if (Objects.nonNull(foodstall)) {

			foodstall.setStatus(status);

			mongoTemplate.save(foodstall);

			return foodstall;
		} else {
			throw new TFException("No Foodstall found with the given foodstall number");
		}

	}
	
	public void updateMerchantStatus(Long uniqueNumber, String status) throws TFException {
		
		Query query = Query.query(Criteria.where("uniqueNumber").is(uniqueNumber));
		
		Merchant merchant = mongoTemplate.findOne(query, Merchant.class);
		
		merchant.setStatus(status);

		mongoTemplate.save(merchant);
		
		System.out.println(merchant);

	}
	
	public FoodStall updateFoodstallStatus(Long uniqueNumber, Long foodstallId, String status) throws TFException {

		FoodStall foodstall = this.getFoodStall(foodstallId);

		if (Objects.nonNull(foodstall)) {

			foodstall.setStatus(status);

			mongoTemplate.save(foodstall);

			return foodstall;
		} else {
			throw new TFException("No Foodstall found with the given foodstall number");
		}

	}
	
	public FoodStall updateSelfQRCode(Long foodstallId, String selfQRCodeUrl) throws TFException {

		FoodStall foodstall = this.getFoodStall(foodstallId);

		if (Objects.nonNull(foodstall)) {

			foodstall.setSelfQrCode(selfQRCodeUrl);

			mongoTemplate.save(foodstall);

			return foodstall;
		} else {
			throw new TFException("No Foodstall found with the given foodstall number");
		}

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

	public BusinessUnit saveBusinessUnit(BusinessUnit businessUnit) {
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
		
		System.out.println("filterMap : " + filterMap);

		if (filterMap.containsKey(BusinessUnitCollectionConstants.NAME)) {
			System.out.println(BusinessUnitCollectionConstants.NAME);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.NAME)
					.is(filterMap.get(BusinessUnitCollectionConstants.NAME)));
		}
		if (filterMap.containsKey(BusinessUnitCollectionConstants.TYPE)) {
			System.out.println(BusinessUnitCollectionConstants.TYPE);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.TYPE)
					.is(filterMap.get(BusinessUnitCollectionConstants.TYPE)));
		}

		if (filterMap.containsKey(BusinessUnitCollectionConstants.CITY)) {
			System.out.println(BusinessUnitCollectionConstants.CITY);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.CITY)
					.is(filterMap.get(BusinessUnitCollectionConstants.CITY)));
		}

		if (filterMap.containsKey(BusinessUnitCollectionConstants.STATUS)) {
			System.out.println(BusinessUnitCollectionConstants.STATUS);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.STATUS)
					.is(filterMap.get(BusinessUnitCollectionConstants.STATUS)));
		}

		if (filterMap.containsKey(BusinessUnitCollectionConstants.STATE)) {
			System.out.println(BusinessUnitCollectionConstants.STATE);
			criteria.add(Criteria.where(BusinessUnitCollectionConstants.STATE)
					.is(filterMap.get(BusinessUnitCollectionConstants.STATE)));
		}

		if (filterMap.containsKey(BusinessUnitCollectionConstants.COUNTRY)) {
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
		
		System.out.println(res);
		return res;
	}
	
	public List<BusinessUnit> getBusinessUnits(String country, String state, String city){
		Query query = new Query(Criteria.where("country").is(country).andOperator(Criteria.where("state").is(state), Criteria.where("city").is(city)));
		
		List<BusinessUnit> buList = mongoTemplate.find(query, BusinessUnit.class);
		
		return buList;
	}

	public Optional<BusinessUnit> findBusinessUnit(Long buId) {
		Query query = new Query(Criteria.where("businessUnitId").is(buId));

		BusinessUnit businessUnit = mongoTemplate.findOne(query, BusinessUnit.class);

		return Optional.ofNullable(businessUnit);
	}
	
	public List<BusinessUnit> findBusinessUnits() {

		List<BusinessUnit> businessUnits = mongoTemplate.findAll(BusinessUnit.class);

		return businessUnits;
	}
	
	public List<FoodCourt> getFoodCourts(Long buId) {
		
		System.out.println(buId);
		
		Query query = new Query(Criteria.where("businessUnitId").is(buId));

		List<FoodCourt> foodCourts = mongoTemplate.find(query, FoodCourt.class);
		
		System.out.println(foodCourts);

		return foodCourts;
	}

	public FoodCourt saveFoodCourt(FoodCourt foodCourt) {

		Long nextFoodCourtSeq = commonSequenceService
				.getFoodCourtNextSequence(MongoCollectionConstant.COLLECTION_FOODCOURT_SEQ);

		foodCourt.setFoodCourtId(nextFoodCourtSeq);

		return mongoTemplate.save(foodCourt);
	}
	
	public void updateFoodCourt(Long foodCourtId, String qrCodeUrl, boolean isQrCodeGenerated) {
		
		Optional<FoodCourt> foodCourtData = this.findFoodCourt(foodCourtId);
		
		if(foodCourtData.isPresent()) {
			FoodCourt foodCourt = foodCourtData.get();
			
			foodCourt.setQRCodeGenerated(isQrCodeGenerated);
			foodCourt.setQrCodeUrl(qrCodeUrl);
			
			mongoTemplate.save(foodCourt);
		}
	}

	public List<FoodCourt> findFoodCourtsByBusinessTypeId(Long buId) {

		Query query = new Query(Criteria.where("businessUnitId").is(buId));
		
		return mongoTemplate.find(query, FoodCourt.class);
	}

	public Optional<FoodCourt> findFoodCourt(Long foodCourtId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("foodCourtId").is(foodCourtId));

		FoodCourt foodCourt = mongoTemplate.findOne(query, FoodCourt.class);
		return Optional.ofNullable(foodCourt);
	}
	
	public List<FoodCourt> findFoodCourts() {

		List<FoodCourt> foodCourts = mongoTemplate.findAll(FoodCourt.class);
		return foodCourts;
	}

	public boolean deleteFoodCourtById(final Long foodCourtId) {
		boolean flag = false;
		mongoTemplate.remove(Query.query(Criteria.where("foodCourtId").is(foodCourtId)), FoodCourt.class);
		flag = true;
		return flag;
	}

	public void correlateFCFS(Long foodStallId, Long foodCourtId) throws TFException {

		Query fcQuery = new Query(Criteria.where(FoodCourtCollectionConstants.FOOD_COURT_NUMBER).is(foodCourtId));
		Query fsQuery = new Query(Criteria.where(FoodStallCollectionConstants.FOOD_STALL_NUMBER).is(foodStallId));

		FoodStall foodStall = mongoTemplate.findOne(fsQuery, FoodStall.class);
		FoodCourt foodCourt = mongoTemplate.findOne(fcQuery, FoodCourt.class);

		if (Objects.isNull(foodCourt)) {
			throw new TFException("Food court not found");
		}

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall not found");
		}

		foodStall.setFoodCourtId(foodCourt.getFoodCourtId());
		foodStall.setFoodCourtName(foodCourt.getName());

		mongoTemplate.save(foodStall);
	}

	public AdminRole saveAdminRole(AdminRole adminRole) {
		return mongoTemplate.save(adminRole);
	}

	public List<AdminRole> findAdminRoles() {
		System.out.println(mongoTemplate.findAll(AdminRole.class));
		return mongoTemplate.findAll(AdminRole.class);
	}

	public AdminRole findRoleByRoleName(String role) {
		Query query = new Query();
		query.addCriteria(Criteria.where("role").is(role));

		AdminRole adminRole = mongoTemplate.findOne(query, AdminRole.class);

		if (!Objects.isNull(adminRole)) {
			adminRole.setRolesConfiguration(this.findRoleConfiguration(role));
		}

		return adminRole;
	}

	public RoleConfiguration findRoleConfiguration(String role) {
		Query query = new Query();
		query.addCriteria(Criteria.where("roleName").is(role));

		RoleConfiguration roleConfiguration = mongoTemplate.findOne(query, RoleConfiguration.class);
		if (Objects.isNull(roleConfiguration)) {
			return new RoleConfiguration();
		} else {
			return roleConfiguration;
		}

	}

	public Admin saveAdmin(Admin admin) {

		return mongoTemplate.save(admin);

	}

	public List<Admin> findAdminUserByRole(String role) {
		Query query = new Query();
		query.addCriteria(Criteria.where("role").is(role));
		List<Admin> admin = mongoTemplate.find(query, Admin.class);
		return admin;
	}

	public Boolean deleteAdminUserByRole(long adminUserId) {

		Boolean flag = false;

		mongoTemplate.remove(Query.query(Criteria.where("adminUserId").is(adminUserId)), Admin.class);

		flag = true;
		return flag;

	}

	public void changePassword(String phoneNumber, String password) throws TFException {

		Optional<Admin> adminData = this.findAdminByPhoneNumber(phoneNumber);

		if (adminData.isPresent()) {
			Admin admin = adminData.get();
			admin.setPassword(password);
			mongoTemplate.save(admin);
		} else {
			throw new TFException("No admin user found with input phone number");
		}

	}

	public RoleConfiguration saveAdminRoleConfiguration(RoleConfiguration roleConfiguration) {
		return roleConfiguration;
	}

	public Admin finAdminByUserId(long adminUserId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("adminUserId").is(adminUserId));
		Admin admin = mongoTemplate.findOne(query, Admin.class);
		return admin;
	}
	
	public AboutUs saveAboutUsData(String data) {
		
		AboutUs aboutUsData = this.getAboutUsData();
		
		if(Objects.isNull(aboutUsData)) {
			aboutUsData = new AboutUs();
			aboutUsData.setActiveId(1L);
		}
		
		aboutUsData.setDescription(data);
		
		mongoTemplate.save(aboutUsData);
		
		return aboutUsData;
	}
	
	public AboutUs getAboutUsData(){
		
		Query query = new Query(Criteria.where("activeId").is(1));
		
		AboutUs data = mongoTemplate.findOne(query, AboutUs.class);
		
		return data;
	}
	
	public Subscription addSubscription(Subscription subscription) {
		mongoTemplate.save(subscription);
		
		return subscription;
	}
	
	public List<Subscription> getExistingSubscriptions(){
		List<Subscription> subscriptions = mongoTemplate.findAll(Subscription.class);
		
		return subscriptions;
	}
	
	public TermsNConditions saveTermsAndConditions(String content) {
		
		TermsNConditions existingContent = this.getTermsAndConditions();
		
		if(Objects.isNull(existingContent)) {
			existingContent = new TermsNConditions();
			existingContent.setActiveId(1L);
		}
		
		existingContent.setDescription(content);
		
		mongoTemplate.save(existingContent);
		
		return existingContent;
	}
	
	public TermsNConditions getTermsAndConditions() {
		Query query = new Query(Criteria.where("activeId").is(1));	
		
		TermsNConditions content = mongoTemplate.findOne(query, TermsNConditions.class);
		
		return content;
	}
}
