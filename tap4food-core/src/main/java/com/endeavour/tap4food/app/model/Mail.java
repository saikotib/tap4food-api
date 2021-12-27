package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "mails")
public class Mail {

	@Id
	private String id;
	
	private String to;
	
	private String from;
	
	private String subject;
	
	private String body;
	
	private String status;
	
	private String mailDate;
}
