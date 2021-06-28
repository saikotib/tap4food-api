package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "merchantUniqueNumbers")
public class UniqueNumber {

	@Id
	private String id;
	
	private Long uniqueNumber;
		
}
