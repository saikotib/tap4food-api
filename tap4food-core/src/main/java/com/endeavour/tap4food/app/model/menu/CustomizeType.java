package com.endeavour.tap4food.app.model.menu;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_MENU_CUSTOMIZE_TYPE)
public class CustomizeType {

	@Id
	private String id;
	
	private String type;
	
	private ZonedDateTime createdTime;
	
	private Boolean visible;
	
	private Long foodStallId;	
	
}
