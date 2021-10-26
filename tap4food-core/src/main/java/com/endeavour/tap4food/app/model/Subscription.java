package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_SUBSCRIPTIONS)
public class Subscription {

	@Id
	private String id;
	
	private String planName;
	
	private String duration;
	
	private String amount;
}
