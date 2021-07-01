package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_EMAIL_TEMPLATES)
public class EmailTemplate {

	@Id
	private String id;
	
	private String templateName;
	
	private String templateBody;
}
