package com.endeavour.tap4food.user.app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.notifications.CustomerNotification;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.NotificationService;

@RestController
@RequestMapping("/api/customer/notifications")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@RequestMapping(value = "/get-pending-notifications", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getPendingNotifications(@RequestParam("phoneNumber") String phoneNumber) {

		List<CustomerNotification> notifications = notificationService.getPendingCustomerNotifications(phoneNumber);

		ResponseEntity<ResponseHolder> response = ResponseEntity
				.ok(ResponseHolder.builder()
						.status("success")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data(notifications)
						.build());
		;

		return response;
	}
	
	@RequestMapping(value = "/mark-notification-as-read", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> markNotificationAsRead(@RequestParam("notificationId") String notificationId) {

		notificationService.markCustomerNotificationAsRead(notificationId);

		ResponseEntity<ResponseHolder> response = ResponseEntity
				.ok(ResponseHolder.builder()
						.status("success")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data("Notification is read")
						.build());
		;

		return response;
	}
}
