package com.endeavour.tap4food.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.repository.FoodStallRepository;

@Service
public class FoodStallService {

	@Autowired
	private FoodStallRepository foodStallRepository;

	public FoodStall createFoodStall(Long merchantUniqNumber, FoodStall foodStall) throws TFException {

		foodStallRepository.createNewFoodStall(merchantUniqNumber, foodStall);

		return foodStall;
	}

	public void addCategory(Long fsId, Category category) throws TFException {

		foodStallRepository.saveCategory(fsId, category);
	}

	public void addSubCategory(Long fsId, @Valid SubCategory subCategory) throws TFException {
		foodStallRepository.saveSubCategory(fsId, subCategory);
	}

	public void editCategory(Long fsId, Category category) throws TFException {

		foodStallRepository.editCategory(fsId, category);
	}

	public void editSubCategory(Long fsId, SubCategory subCategory) throws TFException {
		foodStallRepository.editSubCategory(fsId, subCategory);
	}

	public void removeCategory(Long fsId, Category category) throws TFException {
		foodStallRepository.removeCategory(fsId, category);
	}

	public void removeSubCategory(Long fsId, SubCategory subCategory) throws TFException {
		foodStallRepository.removeSubCategory(fsId, subCategory);
	}

	public void hideCategory(Long fsId, Category category) throws TFException {
		if (category.getVisible().equals(false)) {
			category.setVisible(false);
			foodStallRepository.saveCategory(fsId, category);
		} else {
			category.setVisible(true);
			foodStallRepository.saveCategory(fsId, category);
		}
	}

	public void hideSubCategory(Long fsId, @Valid SubCategory subCategory) throws TFException {
		if (subCategory.getVisible().equals(false)) {
			subCategory.setVisible(false);
			foodStallRepository.saveSubCategory(fsId, subCategory);
		} else {
			subCategory.setVisible(true);
			foodStallRepository.saveSubCategory(fsId, subCategory);
		}
	}

	public List<Category> getAllCategories(Long fsId) throws TFException {
		Optional<List<Category>> categoryId = foodStallRepository.findAllCategories(fsId);
		if (categoryId.isPresent()) {

			return categoryId.get();
		} else {
			return new ArrayList<Category>();
		}
	}

	public List<SubCategory> getAllSubCategories(Long fsId) throws TFException {
		Optional<List<SubCategory>> categoriesList = foodStallRepository.findAllSubCategories(fsId);
		if (categoriesList.isPresent()) {

			return categoriesList.get();

		} else {
			return new ArrayList<SubCategory>();
		}
	}

	public void addCustomizeType(Long fsId, @Valid CustomizeType customizeType) throws TFException {

		foodStallRepository.saveCustomizeType(fsId, customizeType);
	}

	public void editCustomizeType(Long fsId, CustomizeType customizeType) throws TFException {
		foodStallRepository.editCustomizeType(fsId, customizeType);
	}

	public void removeCustomizeType(Long fsId, CustomizeType customizeType) throws TFException {
		foodStallRepository.removeCustomizeType(fsId, customizeType);
	}

	public void hideCustomizeType(Long fsId, @Valid CustomizeType customizeType) throws TFException {
		if (customizeType.getVisible().equals(false)) {
			customizeType.setVisible(false);
			foodStallRepository.saveCustomizeType(fsId, customizeType);

		} else {
			customizeType.setVisible(true);
			foodStallRepository.saveCustomizeType(fsId, customizeType);
		}

	}

	public void addCuisineName(Long fsId, @Valid Cuisine cuisine) throws TFException {
		
		foodStallRepository.saveCuisine(fsId, cuisine);
	}

	public void editCusine(Long fsId, Cuisine cuisine) throws TFException {
		foodStallRepository.editCuisine(fsId, cuisine);

	}

	public void removeCustomizeType(Long fsId, Cuisine cuisine) throws TFException {
		foodStallRepository.removeCuisine(fsId, cuisine);

	}

	public void hideCustomizeType(Long fsId, @Valid Cuisine cuisine) throws TFException {

		if (cuisine.getVisible().equals(false)) {
			cuisine.setVisible(false);
			foodStallRepository.saveCuisine(fsId, cuisine);

		} else {
			cuisine.setVisible(true);
			foodStallRepository.saveCuisine(fsId, cuisine);
		}
	}

	public List<Cuisine> getAllCuisines(Long fsId) throws TFException {
		Optional<List<Cuisine>> cuisines = foodStallRepository.findAllCuisines(fsId);
		if (cuisines.isPresent()) {

			return cuisines.get();
		} else {
			return new ArrayList<Cuisine>();
		}
	}
}
