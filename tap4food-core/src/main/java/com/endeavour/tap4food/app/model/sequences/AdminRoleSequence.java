package com.endeavour.tap4food.app.model.sequences;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_ADMINUSER_SEQ)
public class AdminRoleSequence {

	
	@Id
	private String id;
	
	private int seq;
}
