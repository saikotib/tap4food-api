package com.endeavour.tap4food.app.model.notifications;

import java.time.LocalDateTime;

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
		
	private String notificationType;
	
	private Long notificationObjectId;
	
	private Long notifTime;
}
