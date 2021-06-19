package com.endeavour.tap4food.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ItemOrder {

	private long orderId;
	
	private long itemId;
	
	private String itemName;
	
	private double totalAmountPaid;
	
	private String paymentMode;
	
	private String orderedTime;
	
	private String tokenNumber;
	
	private long merchandId;
	
	private String transactionNumber;
	
	private String orderStatus;
}
