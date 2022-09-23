package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "permissions")
public class Access {
	
	@Id
	private String id;
	
	private String screenName;
	
	private boolean hasAccess;
	
//	private boolean hasEditAccess;

}
