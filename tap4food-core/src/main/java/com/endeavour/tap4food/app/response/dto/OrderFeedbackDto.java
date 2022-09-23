package com.endeavour.tap4food.app.response.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderFeedbackDto {

	private String customerName;
	
	private String email;
	
	private String phoneNumber;
	
	private String orderDate;
	
	private Long orderId;
	
	private List<String> items;
	
	private String review;
	
	private double ratingVal;
}
