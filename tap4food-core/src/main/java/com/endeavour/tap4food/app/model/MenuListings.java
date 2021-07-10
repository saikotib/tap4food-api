package com.endeavour.tap4food.app.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;

import lombok.Data;

@Data
@Document(collection = "menu")
public class MenuListings {

	private String id;
	
	private List<Category> categories;
	
	private List<SubCategory> subCategories;
	
	private List<CustomizeType> customiseType;
	
	private List<Cuisine> cuisines;
	
	private String status;
}
