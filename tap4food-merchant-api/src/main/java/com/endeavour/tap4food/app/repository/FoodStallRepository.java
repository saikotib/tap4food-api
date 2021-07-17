package com.endeavour.tap4food.app.repository;

import java.util.ArrayList;
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
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;

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
	public void saveCategory(Long fsId, @Valid Category menuCategory) throws TFException {

			FoodStall foodStall = this.getFoodStallById(fsId);
	
			if (Objects.isNull(foodStall)) {
				throw new TFException("Food stall is not found.");
			}
	
			if (!StringUtils.hasText(menuCategory.getCategory())) {
				throw new TFException("Invalid category name");
			}
			//menuCategory = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("category").is(menuCategory.getCategory())), Category.class);
			//findAllCategories(fsId);
			try {
				mongoTemplate.save(menuCategory);
			}catch (Exception e) {
				throw new TFException("Category is alraedy available");
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
	public void saveSubCategory(Long fsId, @Valid SubCategory subCategory) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(subCategory.getSubCategory())) {
			throw new TFException("Invalid sub-category name");
		}
		try {
			mongoTemplate.save(subCategory);
		} catch (Exception e) {
			throw new TFException("Sub category is alraedy available");
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
	public void saveCustomizeType(Long fsId, @Valid CustomizeType customizeType) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(customizeType.getType())) {
			throw new TFException("Invalid customize type");
		}

		mongoTemplate.save(customizeType);

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

	public void deleteCategory(@Valid Category category) {

		mongoTemplate.remove(category);
	}

	public void deleteSubCategory(@Valid SubCategory subCategory) {

		mongoTemplate.remove(subCategory);
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

	public void removeCustomizeType(Long fsId, @Valid CustomizeType customizeType) {
		mongoTemplate.remove(customizeType);
	}

	@Transactional
	public void saveCuisine(Long fsId, @Valid Cuisine cuisine) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found.");
		}

		if (!StringUtils.hasText(cuisine.getName())) {
			throw new TFException("Invalid customize type");
		}

		mongoTemplate.save(cuisine);

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

	public void removeCuisine(Long fsId, @Valid Cuisine cuisine) {
		mongoTemplate.remove(cuisine);
	}

	public Optional<List<Cuisine>> findAllCuisines(Long fsId) throws TFException {

		FoodStall foodStall = this.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		}

		List<Cuisine> cuisines = foodStall.getMenuListing().getCuisines();

		return Optional.ofNullable(cuisines);
	}
	

	public void editCategory(Long fsId, @Valid Category category) throws TFException {
		FoodStall foodStall = this.getFoodStallById(fsId);
		
		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall doesn't exist");
		} 
		
		Query query = new Query(Criteria.where("id").is(category.getId()));
		Update updated = new Update().set("category", category.getCategory());
		mongoTemplate.findAndModify(query, updated, Category.class);
		
		Category categoryFromDb = this.findCategoryById(category);
		
		List<Category> categories = foodStall.getMenuListing().getCategories();
		
		System.out.println("categories==> " + categories);
		
		for (Category childCategory : categories) {
			String id = childCategory.getId();
			if (id.equals(categoryFromDb.getId())) {
				categories.remove(childCategory);
				categories.add(categoryFromDb);
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
		
		Query query = new Query().addCriteria(Criteria.where("id").is(subCategory.getId()));
		Update updated = new Update().set("subCategory", subCategory.getSubCategory());
		mongoTemplate.findAndModify(query, updated, SubCategory.class);
		
		SubCategory subCategoryFromDb = this.findSubCategoryById(subCategory);
		
		List<SubCategory> subCategories = foodStall.getMenuListing().getSubCategories();
		
		for (int i = 0; i < subCategories.size(); i++) {
			String id = subCategories.get(i).getId();
			if (id.equals(subCategoryFromDb.getId())) {
				//int index = categories.indexOf(updateCategory);
				subCategories.set(i, subCategoryFromDb);	
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
		
		Query query = new Query().addCriteria(Criteria.where("id").is(cuisine.getId()));
		Update updated = new Update().set("name", cuisine.getName());
		mongoTemplate.findAndModify(query, updated, Cuisine.class);
		
		Cuisine cuisineNameFromDb = this.findCuisineById(cuisine);
		
		List<Cuisine> cuisines = foodStall.getMenuListing().getCuisines();
		
		for (int i = 0; i < cuisines.size(); i++) {
			String id = cuisines.get(i).getId();
			if (id.equals(cuisineNameFromDb.getId())) {
				//int index = categories.indexOf(updateCategory);
				cuisines.set(i, cuisineNameFromDb);	
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
		
		Query query = new Query().addCriteria(Criteria.where("id").is(customizeType.getId()));
		Update updated = new Update().set("type", customizeType.getType());
		mongoTemplate.findAndModify(query, updated, CustomizeType.class);
		
		CustomizeType customizeTypesFromDb = this.findCustomizeTypeById(customizeType);
		
		List<CustomizeType> types = foodStall.getMenuListing().getCustomiseType();
		
		for (int i = 0; i < types.size(); i++) {
			String id = types.get(i).getId();
			if (id.equals(customizeTypesFromDb.getId())) {
				//int index = categories.indexOf(updateCategory);
				types.set(i, customizeTypesFromDb);	
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
}
