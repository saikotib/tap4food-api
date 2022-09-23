package com.endeavour.tap4food.app.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "roleConfiguration")
public class RoleConfiguration {

	@Id
	private String id;

	private String roleName;

	@DBRef
	private List<Access> accessDetails;
}
