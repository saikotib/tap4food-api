package com.endeavour.tap4food.app.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "contactUs")
public class ContactUs {

	@Id
	private String id;
	
	private String fullName;
	
	private String email;
	
	private String phoneNumber;
	
	private String message;
	
	private LocalDateTime time;
}
