package com.endeavour.tap4food.app.model.menu;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_MENU_SUB_CATEGORIES)
public class SubCategory {

	@Id
	private String id;
	
	@Indexed(unique = true)
	private String subCategory;
	
	private ZonedDateTime createdTime;
	
	private Boolean visible = true;
}
