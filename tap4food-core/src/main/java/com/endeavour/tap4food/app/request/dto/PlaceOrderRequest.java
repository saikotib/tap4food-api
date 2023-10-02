package com.endeavour.tap4food.app.request.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequest {

	private Double subTotalAmount;

	@JsonProperty("cTaxAmount")
	private Double cTaxAmount;
	@JsonProperty("sTaxAmount")
	private Double sTaxAmount;

	private Double grandTotal;
	
	private Double packagingPrice;

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
		
		@JsonProperty("isOffer")
		private boolean isOffer;
		
		private boolean customizationFlag;
		
		private List<CartItemCustomization> customizations;
		
		private List<SelectedOfferItem> offerItems;
	}
	
	@Data
	public static class SelectedOfferItem {
		
		private Long itemId;
		
		private String itemName;
		
		private String combination;
		
		private Double actualPrice;
		
		private Double offerPrice;
		
		private long quantity;
	}
	
	@Data
	public static class CartItemCustomization {
		
		private String key;

		private String item;

		private int order;
	}
	
	@Data
	public static class Customer {
		
		private String email;
		
		private String fullName;
		
		private String phoneNumber;
	}
}
