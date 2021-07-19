package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.Data;

@Data
@Document(collection = "adminRoles")
public class AdminRole {

	
	@Id
	private String id;
	
	private String role;
	
	private String description;
}
