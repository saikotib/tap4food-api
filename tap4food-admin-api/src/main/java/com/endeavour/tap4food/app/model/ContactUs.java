package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "enquiries")
public class ContactUs {

	@Id
	private String id;
	
	private String fullName;
	
	private String email;
	
	private String phone;
	
	private String comments;
}
