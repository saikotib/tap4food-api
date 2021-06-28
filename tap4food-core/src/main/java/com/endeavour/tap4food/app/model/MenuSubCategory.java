package com.endeavour.tap4food.app.model;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "menuSubCategories")
public class MenuSubCategory {

	@Id
	private String id;
	
	private String subCategory;
	
	private ZonedDateTime createdTime;
}
