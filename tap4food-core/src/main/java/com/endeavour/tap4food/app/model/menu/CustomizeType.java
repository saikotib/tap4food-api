package com.endeavour.tap4food.app.model.menu;

import java.time.ZonedDateTime;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "menuCustomizeDetails")
public class CustomizeType {

	@Id
	private String id;
	
	private String type;
	
	private ZonedDateTime createdTime;
	
	private Set<String> customizeFoodItems;
	
	private Boolean visible;
}
