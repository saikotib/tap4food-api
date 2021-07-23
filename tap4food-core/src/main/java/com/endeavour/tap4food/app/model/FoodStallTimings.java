package com.endeavour.tap4food.app.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Data;

@Data
//@Document(collection = "foodStallTimings")
public class FoodStallTimings {

	/*
	 * @Id private String id;
	 */
	
	private Long foodStallId;
	
	@DBRef
	private List<WeekDay> days;
	
}
