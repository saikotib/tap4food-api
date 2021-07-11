package com.endeavour.tap4food.app.model.menu;

import java.time.ZonedDateTime;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_MENU_CUSTOMIZE_TYPE)
public class CustomizeType {

	@Id
	private String id;
	@Indexed(unique = true)
	private String type;
	
	private ZonedDateTime createdTime;
	
	private Set<String> customizeFoodItems;
	
	private Boolean visible;
}
