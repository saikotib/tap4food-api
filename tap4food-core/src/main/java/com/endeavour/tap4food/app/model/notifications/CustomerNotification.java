package com.endeavour.tap4food.app.model.notifications;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "pendingCustomerNotofications")
public class CustomerNotification {

	@Id
	private String notificationId;
	
	private Long foodStallId;
	
	private String stallName;
	
	private String customerPhoneNumber;
	
	private String message;
	
	private String notificationStatus;
		
	private String notificationType;
	
	private Long notificationObjectId;
}
