package com.endeavour.tap4food.app.response.dto;

import java.util.List;

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
	
	private Long foodStallId;
	
	private String foodStallName;
	
	private String seatNumber;
	
	private String screen;
	
	private String tableNumber;
	
	private List<OrderedItem> orderedItems;
	
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
