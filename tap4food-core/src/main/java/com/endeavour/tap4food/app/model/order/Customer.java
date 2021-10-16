package com.endeavour.tap4food.app.model.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_ORDER_CUSTOMERS)
public class Customer {
	
	@Id
	private Long id;

	private String email;
	
	private String fullName;
	
	private String phoneNumber;
	
	private Long orderId;
}
