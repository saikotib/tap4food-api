package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_FOODSTALL_SUBSCRIPTIONS)
public class FoodStallSubscription {

	@Id
	private String id;
	
	private Long stallId;
	
	private String stallName;
	
	private String planName;
	
	private String startDate;
	
	private String endDate;
	
	private String status;

}
