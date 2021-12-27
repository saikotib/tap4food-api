package com.endeavour.tap4food.app.model.location;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_COUNTRY)
public class Country {

	private String id;
	
	private String name;
	
	private String countryCode;
	
}
