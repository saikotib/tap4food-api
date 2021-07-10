package com.endeavour.tap4food.app.model.menu;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_MENU_CUISINE)
public class Cuisine {

	@Id
	private String id;
	
	private String name;
	
	private Boolean visible;
}
