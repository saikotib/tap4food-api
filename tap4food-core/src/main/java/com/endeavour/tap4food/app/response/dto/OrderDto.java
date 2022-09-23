package com.endeavour.tap4food.app.response.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderDto {

	private Long orderId;
	
	private String status;
	
	private String customerPhoneNumber;
	
	private String customerName;
	
	private Long orderNumber;
	
	private boolean selfPickup;
	
	private Integer totalItems;
	
	private Double totalAmount;
	
	private Double subTotal;
	
	private Long foodStallId;
	
	private String foodStallName;
	
	private String seatNumber;
	
	private String screen;
	
	private String tableNumber;
	
	private String orderedTime;
	
	private String paymentId;
	
	private List<OrderedItem> orderedItems;
	
	@JsonProperty("isOtpVerified")
	private boolean isOtpVerified;
	
	private Double tax;
	
	private Double taxAmount;
	
	@Data
	public static class OrderedItem{
		
		private Long itemId;
		
		private Integer quantity;
		
		private Double price;
		
		private String itemName;
		
		private boolean customizationFlag;
		
		private List<CustomizationItem> customizations;
	}
	
	@Data
	public static class CustomizationItem{
		
		private String name;
		
		private List<String> items;
	}
}
