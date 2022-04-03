package com.endeavour.tap4food.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.notifications.CustomerNotification;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.repository.NotificationRepository;

@Service
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;
	
	public void addNotification(MessageNotification notification) {
		
		notificationRepository.saveNotification(notification);
	}
	
	public void addCustomerNotification(CustomerNotification notification) {
		
		notificationRepository.saveCustomerNotification(notification);
	}

	public List<MessageNotification> getPendingNotifications(Long foodStallId) {

		List<MessageNotification> notifications = notificationRepository.getPendingNotifications(foodStallId);
		
		for(MessageNotification notif : notifications) {
			notificationRepository.markNotificationAsFetched(notif.getNotificationId());
		}
		
		return notifications;
	}

	public void markNotificationAsRead(String notificationId) {
		System.out.println("In Service");
		notificationRepository.markNotificationAsRead(notificationId);
	}
	
	public void markCustomerNotificationAsRead(String notificationId) {
		notificationRepository.markCustomerNotificationAsRead(notificationId);
	}
	
	public List<CustomerNotification> getPendingCustomerNotifications(String phoneNumber) {

		List<CustomerNotification> notifications = notificationRepository.getPendingCustomerNotifications(phoneNumber); 
		
		for(CustomerNotification notif : notifications) {
			notificationRepository.markCustomerNotificationAsFetched(notif.getNotificationId());
		}
		
		return notifications;
	}
}
