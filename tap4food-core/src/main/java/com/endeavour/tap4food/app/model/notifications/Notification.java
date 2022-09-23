package com.endeavour.tap4food.app.model.notifications;

import lombok.Data;

@Data
public class Notification {

	private String message;
	
	private String status;
	
	private String sender;
	
	private String date;
	
	private String reciever;
}
