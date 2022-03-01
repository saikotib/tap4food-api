package com.endeavour.tap4food.app.model.order;

import lombok.Data;

@Data
public class UpdatePaymentDetailsRequest {

	private Long orderId;
	
	private String paymentId;
	
	private String paymentSignature;
	
	private String paymentStatus;
	
	private String rzpOrderId;
}
