package com.endeavour.tap4food.app.request.dto;

import java.util.List;

import lombok.Data;

@Data
public class PlaceOrderRequest {

	private Double subTotalAmount;

	private Double taxAmount;

	private Double grandTotal;

	private boolean selfPickup;
	
	private boolean isTheatre;

	private String screenNumber;

	private String seatNumber;
	
	private Long foodStallId;
	
	private List<SelectedCartItem> cartItems;
	
	private Customer customer;

	@Data
	public static class SelectedCartItem {
		
		private Long foodItemId;
		
		private String itemName;
		
		private Double finalPrice;
		
		private Integer quantity;
		
		private boolean isPizza;
		
		private String appliedOfferName;
		
		private Long appliedOfferId;
		
		private boolean customizationFlag;
		
		private List<CartItemCustomization> customizations;
	}
	
	@Data
	public static class CartItemCustomization {
		
		private String customizationName;

		private String customizationItem;

		private Double price;
	}
	
	@Data
	public static class Customer {
		
		private String email;
		
		private String fullName;
		
		private String phoneNumber;
	}
}
