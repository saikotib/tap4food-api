package com.endeavour.tap4food.merchant.app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.model.notifications.MessageNotificationsResponse;
import com.endeavour.tap4food.app.model.notifications.Notification;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.NotificationService;
import com.endeavour.tap4food.merchant.app.service.NotificationClientService;

@RestController
@RequestMapping("/api/merchant/notifications")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private NotificationClientService notificationClientService;

	@RequestMapping(value = "/get-pending-notifications", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getPendingNotifications(@RequestParam("fsId") Long fsId) {

		MessageNotificationsResponse notifications = notificationService.getPendingNotifications(fsId);

		ResponseEntity<ResponseHolder> response = ResponseEntity
				.ok(ResponseHolder.builder()
						.status("Merchant Details retrieved succesfully")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data(notifications)
						.build());
		;

		return response;
	}
	
	@RequestMapping(value = "/mark-notification-as-read", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> markNotificationAsRead(@RequestParam("notificationId") String notificationId) {

		System.out.println("In controller");
		notificationService.markNotificationAsRead(notificationId);

		ResponseEntity<ResponseHolder> response = ResponseEntity
				.ok(ResponseHolder.builder()
						.status("Merchant Details retrieved succesfully")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data("Notification is read")
						.build());
		;

		return response;
	}
	
	@RequestMapping(value = "/sendNotificationToMerchant", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> sendNotification(@RequestBody Notification notification) {

		System.out.println("In sendNotification()");
		
		notificationClientService.sendMessageToMerchantGUI(notification);

		ResponseEntity<ResponseHolder> response = ResponseEntity
				.ok(ResponseHolder.builder()
						.status("Notification is sent to merchant")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data("Notification is read")
						.build());
		;

		return response;
	}
}
