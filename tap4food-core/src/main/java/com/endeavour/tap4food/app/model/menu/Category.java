package com.endeavour.tap4food.app.model.menu;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "menuCategories")
public class Category {

	@Id
	private String id;
	
	private String category;
	
	private Boolean visible;
	
}
