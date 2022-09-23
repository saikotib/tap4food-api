package com.endeavour.tap4food.app.model;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "orderFeedback")
public class OrderFeedback {
	
	@Id
	private String id;

	private Long orderId;
	
	private Long foodStallId;
	
	private String review;
	
	private Map<Long, Integer> ratings;
	
	private String customerPhoneNumber;
	
	private String email;
	
}