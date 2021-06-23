package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.enums.OrderStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class ItemOrder {

	@Id
	private String id;
	
	private Long orderNumber;
	
	private Long itemId;
	
	private String itemName;
	
	private Double totalAmountPaid;
	
	private String paymentMode;
	
	private String orderedTime;
	
	private String tokenNumber;
	
	private Long merchandId;
	
	private String transactionNumber;
	
	private OrderStatusEnum orderStatus;
}
