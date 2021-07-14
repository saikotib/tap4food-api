package com.endeavour.tap4food.app.model.location;

import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_CITY)
public class City {

	private String id;

	private String name;

}
