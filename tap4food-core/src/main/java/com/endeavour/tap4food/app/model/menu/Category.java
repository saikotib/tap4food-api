package com.endeavour.tap4food.app.model.menu;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_MENU_CATEGORIES)
public class Category {

	@Transient
	 public static final String SEQUENCE_NAME = "users_sequence";
	
	@Id
	private String id;
	
	//@Indexed(unique = true)
	private String category;
	
	private Boolean visible = true;
	
}
