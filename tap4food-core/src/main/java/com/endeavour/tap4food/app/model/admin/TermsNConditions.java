package com.endeavour.tap4food.app.model.admin;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_TERMS_N_CONDITIONS)
public class TermsNConditions {

	@Id
	private String id;
			
	private String description;
	
	private Long activeId;
}
