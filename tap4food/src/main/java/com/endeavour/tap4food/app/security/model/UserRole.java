package com.endeavour.tap4food.app.security.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.enums.UserRoleEnum;

import lombok.Data;

@Data
@Document(collection = "userRoles")
public class UserRole {

	@Id
	private String id;
	
	private UserRoleEnum name;
	
}
