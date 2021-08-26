package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Document(collection = "otp")
public class Otp {

	@Id
	@JsonIgnore
	private String id;
	
	private String phoneNumber;
	
	private String otp;
	
	private Boolean isExpired;
	
	private Integer numberOfTries;
	
	private Long otpSentTimeInMs;
	
	private String userType;
	
//	private ZonedDateTime optSentTime;
}
