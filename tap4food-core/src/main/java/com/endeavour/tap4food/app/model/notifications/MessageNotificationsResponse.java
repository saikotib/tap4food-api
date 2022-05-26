package com.endeavour.tap4food.app.model.notifications;

import java.util.List;

import lombok.Data;

@Data
public class MessageNotificationsResponse {

	private Long activeCount;
	
	private List<MessageNotification> notifications;
}
