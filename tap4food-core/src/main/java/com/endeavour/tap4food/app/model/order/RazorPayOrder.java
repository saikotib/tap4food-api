package com.endeavour.tap4food.app.model.order;

import java.util.Date;

import lombok.Data;

@Data
public class RazorPayOrder {

	private String orderId;
	
	private int amount;
	
	private Date createdTimeMs;
	
	private int amountDue;
	
	private String currency;
	
	private String reciept;
	
}
