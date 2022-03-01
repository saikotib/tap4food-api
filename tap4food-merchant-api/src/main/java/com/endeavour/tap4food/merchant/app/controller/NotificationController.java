package com.endeavour.tap4food.merchant.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.endeavour.tap4food.app.model.notifications.Notification;

//@Controller
public class NotificationController {

//	@Autowired
//	private SimpMessagingTemplate simpMessagingTemplate;
//
//	@MessageMapping("/message")
//	@SendTo("/tfChannel/public")
//	public Notification receiveMessage(@Payload Notification message) {
//		System.out.println(message);
//		return message;
//	}
//
//	@MessageMapping("/private-notification")
//	public Notification recMessage(@Payload Notification message) {
//		System.out.println(message);
//		simpMessagingTemplate.convertAndSendToUser(message.getReciever(), "/private", message);
//		System.out.println(message.toString());
//		return message;
//	}
}
