package com.endeavour.tap4food.app.model.order;

import lombok.Data;

@Data
public class PaytmReceipt {

	private String txnId;
	
	private String bankName;
	
	private String bankTxnId;
	
	private String currency;
	
	private String gateWayName;
	
	private String mid;
	
	private Long orderId;
	
	private String paymentMode;
	
	private String respCode;
	
	private String responseMessage;
	
	private String status;
	
	private Double txnAmount;
	
	private String txnDate;

	private String checksum;
	
}
