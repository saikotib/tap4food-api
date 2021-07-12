package com.endeavour.tap4food.app.model;

import java.util.List;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "foodCourt")
public class FoodCourt {

	@Id
	private String id;
	
	private String foodCourtId;
	
	private String businessUnitId;
	
	private String name;
	
	private Binary logo;
	
	@DBRef
	private List<FoodStall> foodStalls;
}
