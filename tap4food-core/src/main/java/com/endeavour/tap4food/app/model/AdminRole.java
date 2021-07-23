package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.Data;

@Data
@Document(collection = "adminRoles")
public class AdminRole {

	
	@Id
	private String id;
	
	@Indexed
	private String role;
	
	private String description;
	
	@DBRef
	private RoleConfiguration rolesConfiguration;
}
