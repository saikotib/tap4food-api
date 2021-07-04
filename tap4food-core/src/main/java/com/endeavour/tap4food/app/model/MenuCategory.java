package com.endeavour.tap4food.app.model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "menuCategories")
public class MenuCategory {

	@Id
	private String id;
	
	private String category;
	
	@DBRef
	private Set<MenuSubCategory> subCategories;
}
