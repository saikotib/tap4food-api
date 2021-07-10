package com.endeavour.tap4food.app.model;

import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "foodStallTimings")
public class FoodStallTimings {

	@Id
	private String id;
	
	private Long merchantId;
	
	private String foodStalltId;
	
	@DBRef
	private List<WeekDay> days;
	
}