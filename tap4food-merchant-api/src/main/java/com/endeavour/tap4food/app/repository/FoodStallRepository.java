package com.endeavour.tap4food.app.repository;

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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.MenuListings;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.collection.constants.FoodStallCollectionConstants;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.service.FoodStalNextSequenceService;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

@Repository
@Transactional
public class FoodStallRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private FoodStalNextSequenceService foodStalNextSequenceService;

	@Autowired
	private MerchantRepository merchantRepository;

	public boolean isFoodStallFound(Long foodStallId) {
		boolean merchantExists = false;

		Query query = new Query(Criteria.where(FoodStallCollectionConstants.FOOD_STALL_NUMBER).is(foodStallId));

		merchantExists = mongoTemplate.exists(query, Merchant.class);

		return merchantExists;
	}

	public FoodStall createNewFoodStall(Long merchantId, FoodStall foodStall) throws TFException {

		Optional<Merchant> merchantData = merchantRepository.findMerchantByUniqueId(merchantId);

		if (!merchantData.isPresent()) {
			throw new TFException("Merchant not found");
		}

		foodStall.setMerchantUniqueNumber(merchantId);
		foodStall.setFoodStallId(getIdForNewFoodStall());

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

	private Long getIdForNewFoodStall() {

		Long foodStallID = foodStalNextSequenceService
				.getNextSequence(MongoCollectionConstant.COLLECTION_FOODSTALL_SEQ);

		return foodStallID;
	}

	public FoodStall getFoodStallById(Long fsId) {
		Query query = new Query(Criteria.where("foodStallId").is(fsId));
		FoodStall foodStall = mongoTemplate.findOne(query, FoodStall.class);

		return foodStall;
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
		
		if (!isCategoryFound(menuCategory.getCategory(), foodStall.getMenuListing())) {
			mongoTemplate.save(menuCategory);
		} else {
			throw new TFException("Category is already available");
		}

		MenuListings menuListings = foodStall.getMenuListing();

		if (Objects.isNull(menuListings)) {
			menuListings = new MenuListings();
		}

		List<Category> categories = menuListings.getCategories();

		if (Objects.isNull(categories)) {
			categories = new ArrayList<Category>();
		}

		categories.add(menuCategory);

		menuListings.setCategories(categories);

		mongoTemplate.save(menuListings);

		foodStall.setMenuListing(menuListings);

		mongoTemplate.save(foodStall);

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
		
		if (!isSubCategoryFound(subCategory.getSubCategory(), foodStall.getMenuListing())) {
			mongoTemplate.save(subCategory);
		} else {
			throw new TFException("Sub Category is already available");
		}

		MenuListings menuListings = foodStall.getMenuListing();

		if (Objects.isNull(menuListings)) {
			menuListings = new MenuListings();
		}

		List<SubCategory> subCategories = menuListings.getSubCategories();

		if (Objects.isNull(subCategories)) {
			subCategories = new ArrayList<SubCategory>();
		}

		subCategories.add(subCategory);

		menuListings.setSubCategories(subCategories);

		mongoTemplate.save(menuListings);

		foodStall.setMenuListing(menuListings);

		mongoTemplate.save(foodStall);
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

		if (!isCustomizeTypeFound(customizeType.getType(), foodStall.getMenuListing())) {
			mongoTemplate.save(customizeType);
		} else {
			throw new TFException("Customize Type is already available");
		}

		MenuListings menuListings = foodStall.getMenuListing();

		if (Objects.isNull(menuListings)) {
			menuListings = new MenuListings();
		}

		List<CustomizeType> customizeTypes = menuListings.getCustomiseType();

		if (Objects.isNull(customizeTypes)) {
			customizeTypes = new ArrayList<CustomizeType>();
		}

		customizeTypes.add(customizeType);

		menuListings.setCustomiseType(customizeTypes);

		mongoTemplate.save(menuListings);

		foodStall.setMenuListing(menuListings);

		mongoTemplate.save(foodStall);
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
	while(itr.hasNext()){
	     category = itr.next();
	    if(category.getId().equalsIgnoreCase(existingCategory.getId())){
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

	public Optional<List<Category>> findAllCategories(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}

		List<Category> categories = foodStall.getMenuListing().getCategories();

		return Optional.ofNullable(categories);
	}

	public Optional<List<SubCategory>> findAllSubCategories(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}

		List<SubCategory> subCategories = foodStall.getMenuListing().getSubCategories();

		return Optional.ofNullable(subCategories);
	}

	public Optional<List<CustomizeType>> findCustomiseTypes(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}

		List<CustomizeType> customiseTypes = foodStall.getMenuListing().getCustomiseType();

		return Optional.ofNullable(customiseTypes);
	}

	public Optional<List<Cuisine>> findCuisines(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}

		List<Cuisine> cuisines = foodStall.getMenuListing().getCuisines();

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

		if (!isCuisineFound(cuisine.getName(), foodStall.getMenuListing())) {
			mongoTemplate.save(cuisine);
		} else {
			throw new TFException("Cuisine is already available");
		}

		MenuListings menuListings = foodStall.getMenuListing();

		if (Objects.isNull(menuListings)) {
			menuListings = new MenuListings();
		}

		List<Cuisine> cuisines = menuListings.getCuisines();

		if (Objects.isNull(cuisines)) {
			cuisines = new ArrayList<Cuisine>();
		}

		cuisines.add(cuisine);

		menuListings.setCuisines(cuisines);

		mongoTemplate.save(menuListings);

		foodStall.setMenuListing(menuListings);

		mongoTemplate.save(foodStall);
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
	

	public void editCategory(Long fsId, Category category) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}
		
		Category existingCategory = this.findCategoryById(category);
		
		if (!category.getCategory().equalsIgnoreCase(existingCategory.getCategory())) {
			Query query = new Query(Criteria.where("id").is(category.getId()));
			Update updated = new Update().set("category", category.getCategory());
			mongoTemplate.findAndModify(query, updated, Category.class);
		} else {
			throw new TFException("Category is already exists.");
		}
		
		Category categoryFromDb = this.findCategoryById(category);
		
		List<Category> categories = foodStall.getMenuListing().getCategories();
		
		System.out.println("categories==> " + categories);

		for (int i = 0; i < categories.size(); i++) {
			String listId = categories.get(i).getId();
			if (listId.equalsIgnoreCase(existingCategory.getId())) {
				categories.set(i, categoryFromDb);
				break;
			}	
		}

    MenuListings menuListing = foodStall.getMenuListing();
		menuListing.setCategories(categories);
		
		System.out.println(menuListing);
		mongoTemplate.save(menuListing);
		foodStall.setMenuListing(menuListing);
	}
	
	public void editSubCategory(Long fsId, @Valid SubCategory subCategory) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		SubCategory subCategoryBefore = this.findSubCategoryById(subCategory);
		if (!subCategoryBefore.getSubCategory().equalsIgnoreCase(subCategory.getSubCategory())) {
			Query query = new Query().addCriteria(Criteria.where("id").is(subCategory.getId()));
			Update updated = new Update().set("subCategory", subCategory.getSubCategory());
			mongoTemplate.findAndModify(query, updated, SubCategory.class);
		} else {
			throw new TFException("Sub category is already exists.");
		}
		
		
		SubCategory subCategoryFromDb = this.findSubCategoryById(subCategory);
		
		List<SubCategory> subCategories = foodStall.getMenuListing().getSubCategories();
		
		for (int i = 0; i < subCategories.size(); i++) {
			String listId = subCategories.get(i).getId();
			if (listId.equalsIgnoreCase(subCategoryFromDb.getId())) {
				subCategories.set(i, subCategoryFromDb);
				break;
			}	
		}
		MenuListings menuListing = foodStall.getMenuListing();
		menuListing.setSubCategories(subCategories);
		System.out.println(menuListing);
		mongoTemplate.save(menuListing);
		foodStall.setMenuListing(menuListing);
	}
	
	public void editCuisine(Long fsId, @Valid Cuisine cuisine) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		Cuisine cuisineNameBefore = this.findCuisineById(cuisine);
		if (!cuisine.getName().equalsIgnoreCase(cuisineNameBefore.getName())) {
			Query query = new Query().addCriteria(Criteria.where("id").is(cuisine.getId()));
			Update updated = new Update().set("name", cuisine.getName());
			mongoTemplate.findAndModify(query, updated, Cuisine.class);
		} else {
			throw new TFException("Cuisine is already exists.");
		}
		
		
		Cuisine cuisineNameFromDb = this.findCuisineById(cuisine);
		
		List<Cuisine> cuisines = foodStall.getMenuListing().getCuisines();
		
		for (int i = 0; i < cuisines.size(); i++) {
			String listId = cuisines.get(i).getId();
			if (listId.equalsIgnoreCase(cuisineNameFromDb.getId())) {
				cuisines.set(i, cuisineNameFromDb);
				break;
			}	
		}
		MenuListings menuListing = foodStall.getMenuListing();
		menuListing.setCuisines(cuisines);
		System.out.println(menuListing);
		mongoTemplate.save(menuListing);
		foodStall.setMenuListing(menuListing);
	}
	
	public void editCustomizeType(Long fsId, @Valid CustomizeType customizeType) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		CustomizeType customizeTypesBefore = this.findCustomizeTypeById(customizeType);
		if (!customizeTypesBefore.getType().equalsIgnoreCase(customizeType.getType())) {
			Query query = new Query().addCriteria(Criteria.where("id").is(customizeType.getId()));
			Update updated = new Update().set("type", customizeType.getType());
			mongoTemplate.findAndModify(query, updated, CustomizeType.class);
		} else {
			throw new TFException("Customize type is already exists.");
		}
		
		
		CustomizeType customizeTypesFromDb = this.findCustomizeTypeById(customizeType);
		
		List<CustomizeType> types = foodStall.getMenuListing().getCustomiseType();
		
		for (int i = 0; i < types.size(); i++) {
			String listId = types.get(i).getId();
			if (listId.equalsIgnoreCase(customizeTypesFromDb.getId())) {
				types.set(i, customizeTypesFromDb);
				break;
			}	
		}
		
		MenuListings menuListing = foodStall.getMenuListing();
		menuListing.setCustomiseType(types);
		System.out.println(menuListing);
		mongoTemplate.save(menuListing);
		foodStall.setMenuListing(menuListing);
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
	
	private Boolean isCategoryFound(String categoryName, MenuListings menuListing) {

		boolean flag = false;
		
		if(Objects.isNull(menuListing) || Objects.isNull(menuListing.getCategories()))
			return flag;
		
		for(Category category : menuListing.getCategories()) {
			if(category.getCategory().equals(categoryName)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private Boolean isSubCategoryFound(String subCategoryName, MenuListings menuListing) {

		boolean flag = false;
		
		if(Objects.isNull(menuListing) || Objects.isNull(menuListing.getSubCategories()))
			return flag;
		System.out.println(menuListing);
		for(SubCategory subCategory : menuListing.getSubCategories()) {
			if(subCategory.getSubCategory().equals(subCategoryName)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private Boolean isCustomizeTypeFound(String customizeType, MenuListings menuListing) {

		boolean flag = false;
		
		if(Objects.isNull(menuListing) || Objects.isNull(menuListing.getCustomiseType()))
			return flag;
		System.out.println(menuListing);
		for(CustomizeType type : menuListing.getCustomiseType()) {
			if(type.getType().equals(customizeType)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private Boolean isCuisineFound(String cuisine, MenuListings menuListing) {

		boolean flag = false;
		
		if(Objects.isNull(menuListing) || Objects.isNull(menuListing.getCuisines()))
			return flag;
		
		for(Cuisine name : menuListing.getCuisines()) {
			if(name.getName().equals(cuisine)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}

}
