package com.endeavour.tap4food.app.model.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_ORDERS)
public class Order {
	
	@Id
	private Long id;
	
	private Long orderId;

	private Double subTotalAmount;
	
	private Double taxAmount;
	
	private Double grandTotal;
	
	private boolean selfPickup;
	
	private Long foodStallId;
	
	private String screenNumber;
	
	private boolean isTheatre;
	
	private String seatNumber;
	
	private String status;
	
	private String orderedTime;
	
	private Integer totalItems;
	
	private String paymentStatus;
	
	private String paymentId;
	
	private String rzpOrderId;
	
	private String paymentSignature;
	
	private RazorPayOrder razorPayOrderDetails;
	
	@JsonProperty("isOtpVerified")
	private boolean isOtpVerified;
}
