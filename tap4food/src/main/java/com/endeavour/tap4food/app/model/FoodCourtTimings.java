package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "foodCourtTimings")
public class FoodCourtTimings {

	@Id
	private String id;
	
	private String foodCourtId;
	
	@DBRef
	private WeekDay monday;
	
	@DBRef
	private WeekDay tuesday;
	
	@DBRef
	private WeekDay wednesday;
	
	@DBRef
	private WeekDay thursday;
	
	@DBRef
	private WeekDay friday;
	
	@DBRef
	private WeekDay saturday;
	
	@DBRef
	private WeekDay sunday;
}
