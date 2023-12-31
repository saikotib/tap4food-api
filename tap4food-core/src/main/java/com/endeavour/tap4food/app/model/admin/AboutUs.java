package com.endeavour.tap4food.app.model.admin;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_ABOUTUS)
public class AboutUs {

	@Id
	private String id;
	
	private Long activeId;
	
	private String description;
	
}
