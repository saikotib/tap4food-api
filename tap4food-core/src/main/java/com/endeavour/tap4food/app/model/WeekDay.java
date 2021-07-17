package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "weekDays")
public class WeekDay {

	@Id
	private String id;
	
	private String weekDayName;
	
	private Boolean opened24Hours;
	
	private Boolean closed;
	
	private String openTime;
	
	private String closeTime;
	
	private Long foodStallId;
}
