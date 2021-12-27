package com.endeavour.tap4food.app.model.notifications;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "pendingNotofications")
public class MessageNotification {

	@Id
	private String notificationId;
	
	private Long foodStallId;
	
	private String customerPhoneNumber;
	
	private String message;
	
	private String notificationStatus;
	
	private Long orderId;
	
	private String orderStatus;
}
