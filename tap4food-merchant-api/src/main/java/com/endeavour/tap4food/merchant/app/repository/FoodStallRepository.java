package com.endeavour.tap4food.merchant.app.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.FoodStallSubscription;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.MenuListings;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.Subscription;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomizationPricing;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustFoodItem;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.service.CommonSequenceService;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

@Repository
@Transactional
public class FoodStallRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private CommonSequenceService commonSequenceService;

	@Autowired
	private MerchantRepository merchantRepository;

	private MenuListings createEmptyMenuListing() {
		MenuListings menuListing = new MenuListings();
		
		menuListing.setCategories(new ArrayList<Category>());
		menuListing.setSubCategories(new ArrayList<SubCategory>());
		menuListing.setCuisines(new ArrayList<Cuisine>());
		menuListing.setCustomiseType(new ArrayList<CustomizeType>());
		
		return menuListing;
	}

	public FoodStall createNewFoodStall(Long merchantId, FoodStall foodStall) throws TFException {

		Optional<Merchant> merchantData = merchantRepository.findMerchantByUniqueId(merchantId);

		if (!merchantData.isPresent()) {
			throw new TFException("Merchant not found");
		}
		
		if(isGSTNumberExists(foodStall.getGstNumber())) {
			throw new TFException("GST Number is already used by another foodstall");
		}

		foodStall.setMerchantId(merchantId);
		foodStall.setFoodStallId(getIdForNewFoodStall());
		
		
		MenuListings menuListings = this.createEmptyMenuListing();

		mongoTemplate.save(menuListings);

		foodStall.setMenuListing(menuListings);

		mongoTemplate.save(foodStall);
		 
		/*
		Merchant merchant = merchantData.get();

		List<FoodStall> foodStalls = merchant.getFoodStalls();

		if (Objects.isNull(foodStalls)) {
			foodStalls = new ArrayList<FoodStall>();
		}

		foodStalls.add(foodStall);

		merchant.setFoodStalls(foodStalls);

		mongoTemplate.save(merchant);
		
		*/

		return foodStall;
	}
	
	public FoodStall updateFoodStall(FoodStall foodStall) throws TFException {
		
		System.out.println("Latest foodStall Data : " + foodStall);

		FoodStall existingStall = getFoodStallById(foodStall.getFoodStallId());
		
		if(ObjectUtils.isEmpty(existingStall)) {
			throw new TFException("Invalid foodstall data");
		}
		
		if(!foodStall.getMerchantId().equals(existingStall.getMerchantId())) {
			throw new TFException("You are not authorised to update this foodstall");
		}

		existingStall.setFoodStallLicenseNumber(foodStall.getFoodStallLicenseNumber());
		existingStall.setFoodStallName(foodStall.getFoodStallName());
		existingStall.setGstNumber(foodStall.getGstNumber());
		existingStall.setState(foodStall.getState());
		existingStall.setCity(foodStall.getCity());
		existingStall.setLocation(foodStall.getLocation());
		existingStall.setManagerId(foodStall.getManagerId());
		existingStall.setDeliveryTime(foodStall.getDeliveryTime());
		
		mongoTemplate.save(existingStall);

		return existingStall;
	}
	
	public FoodStall updateFoodStallPic(FoodStall foodStall) throws TFException {

		mongoTemplate.save(foodStall);

		return foodStall;
	}

	private Long getIdForNewFoodStall() {

		Long foodStallID = commonSequenceService
				.getFoodStallNextSequence(MongoCollectionConstant.COLLECTION_FOODSTALL_SEQ);

		return foodStallID;
	}

	public FoodStall getFoodStallById(Long fsId) {
		Query query = new Query(Criteria.where("foodStallId").is(fsId));
		FoodStall foodStall = mongoTemplate.findOne(query, FoodStall.class);

		if(foodStall.getTax() == null) {
			foodStall.setTax(Double.valueOf(5));
		}
		
		return foodStall;
	}
	
	public FoodStall updateFoodstallStatus(Long foodstallId, String status) throws TFException {
		FoodStall foodstall = this.getFoodStallById(foodstallId);
		
		if(Objects.nonNull(foodstall)) {
			foodstall.setStatus(status);
		}else {
			throw new TFException("No foodstall found");
		}
		
		return foodstall;
	}
	
	public FoodStall updateTax(Long foodstallId, Double tax) throws TFException {
		FoodStall foodstall = this.getFoodStallById(foodstallId);
		
		if(Objects.nonNull(foodstall)) {
			foodstall.setTax(tax);
			mongoTemplate.save(foodstall);
		}else {
			throw new TFException("No foodstall found");
		}
		
		return foodstall;
	}
	
	public FoodStall updateFoodstallOpenStatus(Long foodstallId, boolean openStatus) throws TFException {
		FoodStall foodstall = this.getFoodStallById(foodstallId);
		
		if(Objects.nonNull(foodstall)) {
			foodstall.setOpened(openStatus);
			mongoTemplate.save(foodstall);
		}else {
			throw new TFException("No foodstall found");
		}
		
		return foodstall;
	}
	
	public List<FoodStall> getFoodStalls(Long merchantId, boolean isManager) {
		if(isManager) {
			Query query = new Query(Criteria.where("managerId").is(merchantId));
			List<FoodStall> foodStalls = mongoTemplate.find(query, FoodStall.class);
			
			return foodStalls;
		}else {
			
			Query query = new Query(Criteria.where("merchantId").is(merchantId));
			List<FoodStall> foodStalls = mongoTemplate.find(query, FoodStall.class);
			
			return foodStalls;
		}
		
	}
	
	public MenuListings getMenuListingByFoodStallId(Long fsId) {
		Query query = new Query(Criteria.where("foodStallId").is(fsId));
		MenuListings menuListing = mongoTemplate.findOne(query, MenuListings.class);

		return menuListing;
	}

	@Transactional
	public void saveCategory(Long fsId, Category menuCategory) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(menuCategory.getCategory())) {
			throw new TFException("Invalid category name");
		}
		
		Optional<List<Category>> categoriesData = this.getAllCategories(fsId);
		
		List<Category> existingCategories = new ArrayList<Category>();
		boolean noCategoryFound = false;
		
		if(categoriesData.isPresent()) {
			existingCategories = categoriesData.get();
		}else {
			noCategoryFound = true;
		}
		
		if (noCategoryFound || !isCategoryFound(menuCategory.getCategory(), existingCategories)) {
			menuCategory.setFoodStallId(fsId);
			menuCategory.setVisible(true);
			mongoTemplate.save(menuCategory);
		} else {
			throw new TFException("Category is already available");
		}

	}

	@Transactional
	public void saveSubCategory(Long fsId, SubCategory subCategory) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(subCategory.getSubCategory())) {
			throw new TFException("Invalid sub-category name");
		}
		
		Optional<List<SubCategory>> categoriesSubData = this.getAllSubCategories(fsId);
		
		List<SubCategory> existingSubCategories = new ArrayList<SubCategory>();
		
		if(categoriesSubData.isPresent()) {
			existingSubCategories = categoriesSubData.get();
		}
		
		if (ObjectUtils.isEmpty(existingSubCategories) || !isSubCategoryFound(subCategory.getSubCategory(), existingSubCategories)) {
			subCategory.setFoodStallId(fsId);
			subCategory.setVisible(true);
			mongoTemplate.save(subCategory);
		} else {
			throw new TFException("Subcategory is already available");
		}
	}

	@Transactional
	public void saveCustomizeType(Long fsId, CustomizeType customizeType) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(customizeType.getType())) {
			throw new TFException("Invalid customize type");
		}

		Optional<List<CustomizeType>> customiseTypeData = this.getAllCustomiseTypes(fsId);
		
		List<CustomizeType> existingCustomiseTypes = new ArrayList<CustomizeType>();
		
		if(customiseTypeData.isPresent()) {
			existingCustomiseTypes = customiseTypeData.get();
		}
		
		if (ObjectUtils.isEmpty(existingCustomiseTypes) || !isCustomizeTypeFound(customizeType.getType(), existingCustomiseTypes)) {
			customizeType.setFoodStallId(fsId);
			customizeType.setVisible(true);
			mongoTemplate.save(customizeType);
		} else {
			throw new TFException("Subcategory is already available");
		}
	}
	
	@Transactional
	public CustFoodItem saveCustomizeFoodItem(Long fsId, String customiseType, CustFoodItem customiseFoodItem) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(customiseType)) {
			throw new TFException("Invalid customize type");
		}
		
		if(!StringUtils.hasText(customiseFoodItem.getFoodItemName())) {
			throw new TFException("Invalid customise food item details.");
		}
		
		Optional<List<CustFoodItem>> customiseFoodItemsData = this.getAllCustomiseFoodItems(fsId);
		
		List<CustFoodItem> existingCustomiseFoodItems = new ArrayList<CustFoodItem>();
		
		if(customiseFoodItemsData.isPresent()) {
			existingCustomiseFoodItems = customiseFoodItemsData.get();
		}
		
		if (ObjectUtils.isEmpty(existingCustomiseFoodItems) || !isCustomizeFoodItemFound(customiseType, customiseFoodItem.getFoodItemName(), existingCustomiseFoodItems)) {
			customiseFoodItem.setFoodStallId(fsId);
			customiseFoodItem.setVisible(true);
			mongoTemplate.save(customiseFoodItem);
		} else {
			throw new TFException("Customise Food item is already available");
		}
		
		return customiseFoodItem;
	}

	public void removeCategory(Long fsId, Category category) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}

		Category existingCategory = this.findCategoryById(category);
		try {
			if (category.getId().equalsIgnoreCase(existingCategory.getId())) {
				mongoTemplate.remove(existingCategory);
			}
		} catch (Exception e) {
			throw new TFException("Category is not available to delete");
		}

		List<Category> categories = foodStall.getMenuListing().getCategories();

		System.out.println("categories==> " + categories);

		Iterator<Category> itr = categories.iterator();
		while (itr.hasNext()) {
			category = itr.next();
			if (category.getId().equalsIgnoreCase(existingCategory.getId())) {
				itr.remove();
				break;
			}
		}
		MenuListings menuListing = foodStall.getMenuListing();
		menuListing.setCategories(categories);
		System.out.println(menuListing);
		mongoTemplate.save(menuListing);
		foodStall.setMenuListing(menuListing);

	}
	
	public void removeSubCategory(Long fsId, SubCategory subCategory) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		SubCategory existingSubCategory = this.findSubCategoryById(subCategory);
		try {
			if (subCategory.getId().equalsIgnoreCase(existingSubCategory.getId())) {
				mongoTemplate.remove(existingSubCategory);
			} 
		} catch (Exception e) {
			throw new TFException("Sub Category is not available to delete");
		}
		
		List<SubCategory> subCategories = foodStall.getMenuListing().getSubCategories();
		
		System.out.println("sub categories==> " + subCategories);

		Iterator<SubCategory> itr = subCategories.iterator();            
		while(itr.hasNext()){
		   subCategory = itr.next();
		    if(subCategory.getId().equalsIgnoreCase(existingSubCategory.getId())){
		        itr.remove();
		        break;
		    }
		}
		
		MenuListings menuListing = foodStall.getMenuListing();
		menuListing.setSubCategories(subCategories);
		System.out.println(menuListing);
		mongoTemplate.save(menuListing);
		foodStall.setMenuListing(menuListing);
	}

	public Optional<List<Category>> getAllCategories(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId));
		
		List<Category> categories = mongoTemplate.find(query, Category.class);

		return Optional.ofNullable(categories);
	}

	public Optional<List<SubCategory>> getAllSubCategories(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId));

		List<SubCategory> subCategories = mongoTemplate.find(query, SubCategory.class);

		return Optional.ofNullable(subCategories);
	}

	public Optional<List<CustomizeType>> getAllCustomiseTypes(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId));

		List<CustomizeType> customiseTypes = mongoTemplate.find(query, CustomizeType.class);

		return Optional.ofNullable(customiseTypes);
	}
	
	public Optional<List<CustFoodItem>> getAllCustomiseFoodItems(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId));

		List<CustFoodItem> customiseFoodItems = mongoTemplate.find(query, CustFoodItem.class);

		return Optional.ofNullable(customiseFoodItems);
	}

	public Optional<List<Cuisine>> getAllCuisines(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId));

		List<Cuisine> cuisines = mongoTemplate.find(query, Cuisine.class);

		return Optional.ofNullable(cuisines);
	}

	public void removeCustomizeType(Long fsId, CustomizeType customizeType) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		CustomizeType existingCustomizeType = this.findCustomizeTypeById(customizeType);
		try {
			if (customizeType.getId().equalsIgnoreCase(existingCustomizeType.getId())) {
				mongoTemplate.remove(existingCustomizeType);
			} 
		} catch (Exception e) {
			throw new TFException("Customize type is not available to delete");
		}
		
		List<CustomizeType> types = foodStall.getMenuListing().getCustomiseType();
		
		System.out.println("customise types==> " + types);

		Iterator<CustomizeType> itr = types.iterator();            
		while(itr.hasNext()){
		   customizeType = itr.next();
		    if(customizeType.getId().equalsIgnoreCase(existingCustomizeType.getId())){
		        itr.remove();
		        break;
		    }
		}
		
		MenuListings menuListing = foodStall.getMenuListing();
		menuListing.setCustomiseType(types);
		System.out.println(menuListing);
		mongoTemplate.save(menuListing);
		foodStall.setMenuListing(menuListing);
	
	}
	
	public void deleteCustomiseFoodItem(String custFoodItemId) {
		
		CustFoodItem custFoodItem = mongoTemplate.findById(custFoodItemId, CustFoodItem.class);
		
		mongoTemplate.remove(custFoodItem);		
	}

	@Transactional
	public void saveCuisine(Long fsId, Cuisine cuisine) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(cuisine.getName())) {
			throw new TFException("Invalid customize type");
		}

		Optional<List<Cuisine>> cuisinesData = this.getAllCuisines(fsId);
		
		List<Cuisine> existingCuisines = new ArrayList<Cuisine>();
		
		if(cuisinesData.isPresent()) {
			existingCuisines = cuisinesData.get();
		}
		
		if (!isCuisineFound(cuisine.getName(), existingCuisines)) {
			cuisine.setFoodStallId(fsId);
			cuisine.setVisible(true);
			mongoTemplate.save(cuisine);
		} else {
			throw new TFException("Category is already available");
		}
	}

	public void removeCuisine(Long fsId, Cuisine cuisine) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Cuisine existingCuisineName = this.findCuisineById(cuisine);
		try {
			if (cuisine.getId().equalsIgnoreCase(existingCuisineName.getId())) {
				mongoTemplate.remove(existingCuisineName);
			} 
		} catch (Exception e) {
			throw new TFException("Cuisine is not available to delete");
		}
		
		List<Cuisine> names = foodStall.getMenuListing().getCuisines();
		
		System.out.println("CuisineNames==> " + names);

		Iterator<Cuisine> itr = names.iterator();            
		while(itr.hasNext()){
		   cuisine = itr.next();
		    if(cuisine.getId().equalsIgnoreCase(existingCuisineName.getId())){
		        itr.remove();
		        break;
		    }
		}
		
		MenuListings menuListing = foodStall.getMenuListing();
		menuListing.setCuisines(names);
		System.out.println(menuListing);
		mongoTemplate.save(menuListing);
		foodStall.setMenuListing(menuListing);
	}

	public Category updateCategory(Long fsId, Category category, boolean isToggle) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Optional<List<Category>> existingCategoriesData = this.getAllCategories(fsId);
		
		List<Category> existingCategories = new ArrayList<Category>();
		
		if(existingCategoriesData.isPresent()) {
			existingCategories = existingCategoriesData.get();
		}
		
		if(!isToggle && isCategoryFound(category.getCategory(), existingCategories)) {
			throw new TFException("This category is already found");
		}
		
		for(Category existingCategory : existingCategories) {
			if(existingCategory.getId().equals(category.getId())) {
				if(isToggle) {
					existingCategory.setVisible(!existingCategory.getVisible());
				}else {
					existingCategory.setCategory(category.getCategory());
				}
				
				mongoTemplate.save(existingCategory);
				
				category = existingCategory;
				break;
			}
		}
		
		return category;
	}
	
	public SubCategory updateSubCategory(Long fsId, SubCategory subCategory, boolean isToggle) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Optional<List<SubCategory>> existingSubCategoriesData = this.getAllSubCategories(fsId);
		
		List<SubCategory> existingSubCategories = new ArrayList<SubCategory>();
		
		if(existingSubCategoriesData.isPresent()) {
			existingSubCategories = existingSubCategoriesData.get();
		}
		
		if(!isToggle && isSubCategoryFound(subCategory.getSubCategory(), existingSubCategories)) {
			throw new TFException("This sub-category is already found");
		}
		
		for(SubCategory existingSubCategory : existingSubCategories) {
			if(existingSubCategory.getId().equals(subCategory.getId())) {
				
				if(isToggle) {
					existingSubCategory.setVisible(!existingSubCategory.getVisible());
				}else {
					existingSubCategory.setSubCategory(subCategory.getSubCategory());
				}
				
				mongoTemplate.save(existingSubCategory);
				subCategory = existingSubCategory;
				break;
			}
		}
		
		return subCategory;
	}
	
	public Cuisine updateCuisine(Long fsId, Cuisine cuisine, boolean isToggle) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		
		Optional<List<Cuisine>> existingCuisinesData = this.getAllCuisines(fsId);
		
		List<Cuisine> existingCuisines = new ArrayList<Cuisine>();
		
		if(existingCuisinesData.isPresent()) {
			existingCuisines = existingCuisinesData.get();
		}
		
		if(!isToggle && isCuisineFound(cuisine.getName(), existingCuisines)) {
			throw new TFException("This cuisine is already found");
		}
		
		for(Cuisine existingCuisine : existingCuisines) {
			if(existingCuisine.getId().equals(cuisine.getId())) {
				
				if(isToggle) {
					existingCuisine.setVisible(!existingCuisine.getVisible());
				}else {
					existingCuisine.setName(cuisine.getName());
				}
				
				mongoTemplate.save(existingCuisine);
				cuisine = existingCuisine;
				break;
			}
		}
		return cuisine;
	}
	
	public CustomizeType updateCustomizeType(Long fsId, CustomizeType customizeType, boolean isToggle) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		
		Optional<List<CustomizeType>> existingCustomiseTypesData = this.getAllCustomiseTypes(fsId);
		
		List<CustomizeType> existingCustomiseTypes = new ArrayList<CustomizeType>();
		
		if(existingCustomiseTypesData.isPresent()) {
			existingCustomiseTypes = existingCustomiseTypesData.get();
		}
		
		if(!isToggle && isCustomizeTypeFound(customizeType.getType(), existingCustomiseTypes)) {
			throw new TFException("This customise type is already found");
		}
		
		for(CustomizeType existingCustType : existingCustomiseTypes) {
			if(existingCustType.getId().equals(customizeType.getId())) {
				
				if(isToggle) {
					existingCustType.setVisible(!existingCustType.getVisible());
				}else {
					existingCustType.setType(customizeType.getType());
				}
				
				mongoTemplate.save(existingCustType);
				customizeType = existingCustType;
				break;
			}
		}
		return customizeType;
	}
	
	public CustFoodItem updateCustomizeFoodItem(Long fsId, CustFoodItem foodItem, boolean isToggle) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		
		Optional<List<CustFoodItem>> existingCustomiseFoodItemsData = this.getAllCustomiseFoodItems(fsId);
		
		List<CustFoodItem> existingCustomiseFoodItems = new ArrayList<CustFoodItem>();
		
		
		if(existingCustomiseFoodItemsData.isPresent()) {
			existingCustomiseFoodItems = existingCustomiseFoodItemsData.get();
			
		}
		
		
		if(!isToggle && isCustomizeFoodItemFound(foodItem.getCustomiseType(), foodItem.getFoodItemName(), existingCustomiseFoodItems)) {
			throw new TFException("This customise food item is already found");
		}
		
		for(CustFoodItem existingCustFoodItem : existingCustomiseFoodItems) {
			if(existingCustFoodItem.getId().equals(foodItem.getId())) {
				
				if(isToggle) {
					existingCustFoodItem.setVisible(!existingCustFoodItem.isVisible());
				}else {
					existingCustFoodItem.setFoodItemName(foodItem.getFoodItemName());
				}
				
				mongoTemplate.save(existingCustFoodItem);
				foodItem = existingCustFoodItem;
				break;
			}
		}
		return foodItem;
	}
	
	public Category findCategoryById(@Valid Category category) {
		Category categoryFromDb = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(category.getId())), Category.class);
		return categoryFromDb;
	}

	public SubCategory findSubCategoryById(@Valid SubCategory subCategory) {
		SubCategory subCategoryFromDb = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(subCategory.getId())), SubCategory.class);
		return subCategoryFromDb;
	}
	
	public Cuisine findCuisineById(@Valid Cuisine cuisine) {
		Cuisine cuisineFromDb = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(cuisine.getId())), Cuisine.class);
		return cuisineFromDb;
	}
	
	public CustomizeType findCustomizeTypeById(@Valid CustomizeType customizeType) {
		CustomizeType customizeTypeFromDb = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(customizeType.getId())), CustomizeType.class);
		return customizeTypeFromDb;
	}
	
	public FoodStallTimings savefoodStallTimings(Long foodStallId, FoodStallTimings foodStallTimings, boolean updateFlag) throws TFException {
		
		FoodStall foodStall = this.getFoodStallById(foodStallId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		
		List<WeekDay> persistedWeekDays = new ArrayList<WeekDay>();
		
		for(WeekDay weekDay : foodStallTimings.getDays()) {
			if(!updateFlag) {
				if(!isWeekDayAvailableForFS(foodStallId, weekDay.getWeekDayName())) {
					weekDay.setFoodStallId(foodStallId);
					mongoTemplate.save(weekDay);
					persistedWeekDays.add(weekDay);
				}else {
					throw new TFException(String.format("The weekday(%s) is already available for the foodstall", weekDay.getWeekDayName()));
				}
			}else {
				mongoTemplate.save(weekDay);
				persistedWeekDays.add(weekDay);
			}
		}
		
		foodStallTimings.setDays(persistedWeekDays);
		
		return foodStallTimings;
		
	}
	
	public FoodStallTimings getFoodStallTimings(Long fsId) {
		FoodStallTimings timings = new FoodStallTimings();
		
		Query query = new Query(Criteria.where("foodStallId").is(fsId));
		
		List<WeekDay> weekdays = mongoTemplate.find(query, WeekDay.class);
		
		timings.setFoodStallId(fsId);
		timings.setDays(weekdays);
		
		return timings;
	}
	
	private Boolean isCategoryFound(String categoryName, List<Category> categories) {

		boolean flag = false;

		for(Category category : categories) {
			if(category.getCategory().equals(categoryName)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private Boolean isSubCategoryFound(String subCategoryName, List<SubCategory> subCategories) {

		boolean flag = false;
		
		for(SubCategory subCategory : subCategories) {
			if(subCategory.getSubCategory().equals(subCategoryName)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private Boolean isCustomizeTypeFound(String customizeType, List<CustomizeType> customiseTypes) {

		boolean flag = false;

		for(CustomizeType type : customiseTypes) {
			if(type.getType().equals(customizeType)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private Boolean isCustomizeFoodItemFound(String customizeType, String itemName, List<CustFoodItem> items) {

		boolean flag = false;

		for(CustFoodItem item : items) {
			if(item.getCustomiseType().equals(customizeType) && item.getFoodItemName().equals(itemName)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private Boolean isCusineFound(String cuisineName, List<Cuisine> cuisines) {

		boolean flag = false;

		for(Cuisine cuisine : cuisines) {
			if(cuisine.getName().equals(cuisineName)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private CustomizeType getCustomizeTypeDetails(String customizeTypeName, List<CustomizeType> existingCustomiseTypes) {

		CustomizeType customiseTypeObject = null;
		
		if(ObjectUtils.isEmpty(existingCustomiseTypes)) {
			return null;
		}
		
		for(CustomizeType type : existingCustomiseTypes) {
			if(type.getType().equals(customizeTypeName)) {
				customiseTypeObject = type;
				break;
			}
		}
		
		return customiseTypeObject;
	}
	
	private CustFoodItem getCustomizeFoodItemDetails(String customizeTypeName, String foodItemName, List<CustFoodItem> existingCustomiseFoodItems) {

		CustFoodItem customiseItemObject = null;
		
		if(ObjectUtils.isEmpty(existingCustomiseFoodItems)) {
			return null;
		}
		
		for(CustFoodItem item : existingCustomiseFoodItems) {
			if(item.getCustomiseType().equals(customizeTypeName) && item.getFoodItemName().equals(foodItemName)) {
				customiseItemObject = item;
				break;
			}
		}
		
		return customiseItemObject;
	}
	
	private Boolean isCuisineFound(String cuisine, List<Cuisine> existingCuisines) {

		boolean flag = false;
		
		if(ObjectUtils.isEmpty(existingCuisines))
			return flag;
		
		for(Cuisine name : existingCuisines) {
			if(name.getName().equals(cuisine)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private boolean isWeekDayAvailableForFS(Long fsId, String weekDay) {
		Query query = new Query(Criteria.where("foodStallId").is(fsId).andOperator(Criteria.where("weekDayName").is(weekDay)));
		
		long weekDaysCount = mongoTemplate.count(query, WeekDay.class);
		
		if(weekDaysCount == 0) {
			return false;
		}else {
			return true;
		}
	}

	public FoodStallSubscription addMerchantSubscriptionDetails(FoodStallSubscription merchantSubscription) {
		
		mongoTemplate.save(merchantSubscription);
		
		return merchantSubscription;
	}
	
	public Subscription getSubscriptionDetails(String subscriptionName) {
		Query query = new Query(Criteria.where("planName").is(subscriptionName));
		Subscription subscriptionDetails = mongoTemplate.findOne(query, Subscription.class);
		
		return subscriptionDetails;
	}
	
	public FoodStallSubscription getMerchantSubscriptionDetails(Long foodStallId) {
		Query query = new Query(Criteria.where("stallId").is(foodStallId));
		FoodStallSubscription subscriptionDetails = mongoTemplate.findOne(query, FoodStallSubscription.class);
		
		return subscriptionDetails;
	}
	
	
	public void deleteFoodItem(Long foodItemId) {
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId).orOperator(Criteria.where("baseItem").is(foodItemId)));
		
		List<FoodItem> foodItems = mongoTemplate.find(query, FoodItem.class);
		
		for(FoodItem foodItem : foodItems) {
			foodItem.setStatus("DELETED");
			mongoTemplate.save(foodItem);
			
			query = new Query(Criteria.where("foodItemId").is(foodItem.getFoodItemId()));
			
			FoodItemPricing itemPricingObject = mongoTemplate.findOne(query, FoodItemPricing.class);
			
			itemPricingObject.setStatus("DELETED");
			
			mongoTemplate.save(itemPricingObject);
			
			if(foodItem.isAvailableCustomisation()) {
				List<FoodItemCustomizationPricing> foodItemCustPricingDetails = mongoTemplate.find(query, FoodItemCustomizationPricing.class);
				
				for(FoodItemCustomizationPricing foodItemCustPricing : foodItemCustPricingDetails) {
					foodItemCustPricing.setStatus("DELETED");
					mongoTemplate.save(foodItemCustPricing);
				}
			}
		}
	}
	
	public boolean isGSTNumberExists(String gstNumber) {
		Query query = new Query(Criteria.where("gstNumber").is(gstNumber));
		
		List<FoodStall> foodStalls = mongoTemplate.find(query, FoodStall.class);
		
		if(ObjectUtils.isEmpty(foodStalls)) {
			return false;
		}else {
			return true;
		}
	}
	
	public Optional<BusinessUnit> findBusinessUnit(Long buId) {
		Query query = new Query(Criteria.where("businessUnitId").is(buId));

		BusinessUnit businessUnit = mongoTemplate.findOne(query, BusinessUnit.class);

		return Optional.ofNullable(businessUnit);
	}
	
	public BusinessUnit saveBusinessUnit(BusinessUnit businessUnit) {
		return mongoTemplate.save(businessUnit);
	}
	
	public FoodCourt saveFoodCourt(FoodCourt foodCourt) {

		Long nextFoodCourtSeq = commonSequenceService
				.getFoodCourtNextSequence(MongoCollectionConstant.COLLECTION_FOODCOURT_SEQ);

		foodCourt.setFoodCourtId(nextFoodCourtSeq);

		return mongoTemplate.save(foodCourt);
	}
}
