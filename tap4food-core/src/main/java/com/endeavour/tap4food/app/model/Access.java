package com.endeavour.tap4food.app.model;



import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "roleConfiguration")
public class Access {
	
	
	private String id;
	
	private String screenName;
	
	private String hasReadAccess;
	
	private String hasWriteAccess;

}
