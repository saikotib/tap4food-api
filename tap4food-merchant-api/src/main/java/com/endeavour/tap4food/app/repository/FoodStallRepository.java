package com.endeavour.tap4food.app.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.MenuListings;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.collection.constants.FoodStallCollectionConstants;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
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

	public boolean isFoodStallFound(Long foodStallId) {
		boolean merchantExists = false;

		Query query = new Query(Criteria.where(FoodStallCollectionConstants.FOOD_STALL_NUMBER).is(foodStallId));

		merchantExists = mongoTemplate.exists(query, Merchant.class);

		return merchantExists;
	}
	
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

		foodStall.setMerchantUniqueNumber(merchantId);
		foodStall.setFoodStallId(getIdForNewFoodStall());
		
		
		MenuListings menuListings = this.createEmptyMenuListing();

		mongoTemplate.save(menuListings);

		foodStall.setMenuListing(menuListings);

		mongoTemplate.save(foodStall);
		 

		Merchant merchant = merchantData.get();

		List<FoodStall> foodStalls = merchant.getFoodStalls();

		if (Objects.isNull(foodStalls)) {
			foodStalls = new ArrayList<FoodStall>();
		}

		foodStalls.add(foodStall);

		merchant.setFoodStalls(foodStalls);

		mongoTemplate.save(merchant);

		return foodStall;
	}
	
	public FoodStall updateFoodStall(FoodStall foodStall) throws TFException {

		FoodStall existingStall = getFoodStallById(foodStall.getFoodStallId());
		
		if(ObjectUtils.isEmpty(existingStall)) {
			throw new TFException("Invalid foodstall data");
		}
		
		if(!foodStall.getMerchantUniqueNumber().equals(existingStall.getMerchantUniqueNumber())) {
			throw new TFException("You are not authorised to update this foodstall");
		}

		existingStall.setFoodStallLicenseNumber(foodStall.getFoodStallLicenseNumber());
		existingStall.setFoodStallName(foodStall.getFoodStallName());
		existingStall.setGstNumber(foodStall.getGstNumber());
		existingStall.setState(foodStall.getState());
		existingStall.setCity(foodStall.getCity());
		existingStall.setLocation(foodStall.getLocation());
		
		mongoTemplate.save(existingStall);

		return existingStall;
	}

	private Long getIdForNewFoodStall() {

		Long foodStallID = commonSequenceService
				.getFoodStallNextSequence(MongoCollectionConstant.COLLECTION_FOODSTALL_SEQ);

		return foodStallID;
	}

	public FoodStall getFoodStallById(Long fsId) {
		Query query = new Query(Criteria.where("foodStallId").is(fsId));
		FoodStall foodStall = mongoTemplate.findOne(query, FoodStall.class);

		return foodStall;
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
			mongoTemplate.save(customizeType);
		} else {
			throw new TFException("Subcategory is already available");
		}
	}
	
	@Transactional
	public void saveCustomizeFoodItem(Long fsId, String customiseName, Map<String, Double> customizeFoodItemMap) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(customiseName)) {
			throw new TFException("Invalid customize type");
		}
		
		if(customizeFoodItemMap.isEmpty()) {
			throw new TFException("Invalid customise food items details.");
		}
		
		Optional<List<CustomizeType>> customiseTypeData = this.getAllCustomiseTypes(fsId);
		
		List<CustomizeType> existingCustomiseTypes = new ArrayList<CustomizeType>();
		
		if(customiseTypeData.isPresent()) {
			existingCustomiseTypes = customiseTypeData.get();
		}
		
		CustomizeType customiseTypeDetails = this.getCustomizeTypeDetails(customiseName, existingCustomiseTypes);
		
		Map<String, Double> existingCustFoodItemMap = customiseTypeDetails.getCustomizeFoodItems();
		
		if(Objects.isNull(existingCustFoodItemMap)) {
			existingCustFoodItemMap = new HashMap<String, Double>();
		}
		
		existingCustFoodItemMap.putAll(customizeFoodItemMap);

		customiseTypeDetails.setCustomizeFoodItems(existingCustFoodItemMap);
		
		mongoTemplate.save(customiseTypeDetails);
		
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

	public Optional<List<Cuisine>> findAllCuisines(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}

		List<Cuisine> cuisines = foodStall.getMenuListing().getCuisines();

		return Optional.ofNullable(cuisines);
	}
	
	public void updateCategory(Long fsId, Category category) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Optional<List<Category>> existingCategoriesData = this.getAllCategories(fsId);
		
		List<Category> existingCategories = new ArrayList<Category>();
		
		if(existingCategoriesData.isPresent()) {
			existingCategories = existingCategoriesData.get();
		}
		
		if(isCategoryFound(category.getCategory(), existingCategories)) {
			throw new TFException("This category is already found");
		}
		
		for(Category existingCategory : existingCategories) {
			if(existingCategory.getId().equals(category.getId())) {
				
				existingCategory.setCategory(category.getCategory());
				mongoTemplate.save(existingCategory);
				
				break;
			}
		}
	}
	
	public void updateSubCategory(Long fsId, SubCategory subCategory) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Optional<List<SubCategory>> existingSubCategoriesData = this.getAllSubCategories(fsId);
		
		List<SubCategory> existingSubCategories = new ArrayList<SubCategory>();
		
		if(existingSubCategoriesData.isPresent()) {
			existingSubCategories = existingSubCategoriesData.get();
		}
		
		if(isSubCategoryFound(subCategory.getSubCategory(), existingSubCategories)) {
			throw new TFException("This sub-category is already found");
		}
		
		for(SubCategory existingSubCategory : existingSubCategories) {
			if(existingSubCategory.getId().equals(subCategory.getId())) {
				
				existingSubCategory.setSubCategory(subCategory.getSubCategory());
				
				mongoTemplate.save(existingSubCategory);
				break;
			}
		}
		
	}
	
	public void updateCuisine(Long fsId, Cuisine cuisine) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		
		Optional<List<Cuisine>> existingCuisinesData = this.getAllCuisines(fsId);
		
		List<Cuisine> existingCuisines = new ArrayList<Cuisine>();
		
		if(existingCuisinesData.isPresent()) {
			existingCuisines = existingCuisinesData.get();
		}
		
		if(isCuisineFound(cuisine.getName(), existingCuisines)) {
			throw new TFException("This cuisine is already found");
		}
		
		for(Cuisine existingCuisine : existingCuisines) {
			if(existingCuisine.getId().equals(cuisine.getId())) {
				
				existingCuisine.setName(cuisine.getName());
				
				mongoTemplate.save(existingCuisine);
				break;
			}
		}
	}
	
	public void updateCustomizeType(Long fsId, CustomizeType customizeType) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		
		Optional<List<CustomizeType>> existingCustomiseTypesData = this.getAllCustomiseTypes(fsId);
		
		List<CustomizeType> existingCustomiseTypes = new ArrayList<CustomizeType>();
		
		if(existingCustomiseTypesData.isPresent()) {
			existingCustomiseTypes = existingCustomiseTypesData.get();
		}
		
		if(isCustomizeTypeFound(customizeType.getType(), existingCustomiseTypes)) {
			throw new TFException("This customise type is already found");
		}
		
		for(CustomizeType existingCustType : existingCustomiseTypes) {
			if(existingCustType.getId().equals(customizeType.getId())) {
				
				existingCustType.setType(customizeType.getType());
				
				mongoTemplate.save(existingCustType);
				break;
			}
		}
	}
	
	public void updateCustomizeFoodItem(Long fsId, String customiseType, Map<String, Double> oldDataMap, Map<String, Double> newDataMap) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		
		Optional<List<CustomizeType>> existingCustomiseTypesData = this.getAllCustomiseTypes(fsId);
		
		List<CustomizeType> existingCustomiseTypes = new ArrayList<CustomizeType>();
		
		if(existingCustomiseTypesData.isPresent()) {
			existingCustomiseTypes = existingCustomiseTypesData.get();
		}
		
		for(CustomizeType existingCustType : existingCustomiseTypes) {
			
			if(existingCustType.getType().equals(customiseType)) {
				
				Map<String, Double> updatedFoodItemDetailsMap = new HashMap<String, Double>();
				
				Map<String, Double> existingFoodItemDetails = existingCustType.getCustomizeFoodItems();
				
				if(!ObjectUtils.isEmpty(existingFoodItemDetails)) {
					
					for(Map.Entry<String, Double> entry : existingFoodItemDetails.entrySet()) {
						if(oldDataMap.containsKey(entry.getKey())) {
							updatedFoodItemDetailsMap.putAll(newDataMap);
						}else {
							updatedFoodItemDetailsMap.put(entry.getKey(), entry.getValue());
						}
					}
				}
				existingCustType.setCustomizeFoodItems(updatedFoodItemDetailsMap);
				mongoTemplate.save(existingCustType);
				break;
			}			
		}
		
		
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

}
