package com.endeavour.tap4food.app.model.order;

import java.util.TreeMap;

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
	
	private Double sTaxAmount;
	
	private Double cTaxAmount;
	
	private Double grandTotal;
		
	private boolean selfPickup;
	
	private Long foodStallId;
	
	private String screenNumber;
	
	private boolean isTheatre;
	
	private String seatNumber;
	
	private String status;
	
	private String orderedTime;
	
	private String timeZone;
	
	private Integer totalItems;
	
	private String paymentStatus;
	
	private String paymentId;
	
	private String transactionId;
	
	 TreeMap<String, String> payTmTransactionParameters;
//	private RazorPayOrder razorPayOrderDetails;
	
	private PaytmReceipt paytmReceipt;
			
	@JsonProperty("isOtpVerified")
	private boolean isOtpVerified;
	
	private Double tax;
	
	private String otp;
	
	private String deliveryTime;
}
