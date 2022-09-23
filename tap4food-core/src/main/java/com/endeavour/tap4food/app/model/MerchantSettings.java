package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "merchantSettings")
public class MerchantSettings {

	@Id
	private String id;
	
	private Long merchantId;
	
	private boolean adminNotification;
	
	private boolean subscriptionNotifications;
	
	private boolean orderNotifications;
	
	private boolean remainderNotifications;
	
	private String printType;
}
