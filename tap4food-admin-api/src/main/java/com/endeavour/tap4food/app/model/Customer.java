package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customers")
public class Customer {

	@Id
	private String id;
	
	private String uniqId;
	
	private String fullName;
	
	private String phoneNumber;
	
	private String email;
	
}
