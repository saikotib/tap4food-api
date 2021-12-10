package com.endeavour.tap4food.user.app.response.dto;

import lombok.Data;

@Data
public class OrderResponseDto {

	private Long orderId;
	
	private String foodStallName;
	
	private String price;
	
	private String orderDate;
	
	private String status;
	
}
