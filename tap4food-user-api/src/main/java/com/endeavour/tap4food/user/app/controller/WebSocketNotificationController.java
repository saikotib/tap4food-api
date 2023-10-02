package com.endeavour.tap4food.user.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.endeavour.tap4food.app.model.notifications.Notification;
import com.endeavour.tap4food.user.app.service.WebSocketNotificationService;

@Controller
@CrossOrigin
public class WebSocketNotificationController {

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	
	@Autowired
	private WebSocketNotificationService webSocketNotificationService;

	@MessageMapping("/message")
	@SendTo("/tfChannel/public")
	public Notification receiveMessage(@Payload Notification message) {
		System.out.println(message);
		return message;
	}

	@MessageMapping("/private-notification")
	public Notification recMessage(@Payload Notification message) {
		System.out.println(message);
//		simpMessagingTemplate.convertAndSendToUser(message.getReciever(), "/private", message);
		
//		webSocketNotificationService.sendNotificationToMerchant(message);
		
		System.out.println(message.toString());
		return message;
	}
}
