package com.endeavour.tap4food.app.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.notifications.CustomerNotification;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;

@Repository

public class NotificationRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public MessageNotification saveNotification(MessageNotification notification) {
		mongoTemplate.save(notification);
		
		return notification;
	}
	
	public CustomerNotification saveCustomerNotification(CustomerNotification notification) {
		mongoTemplate.save(notification);
		
		return notification;
	}
	
	public List<MessageNotification> getPendingNotifications(Long foodStallId){
		
		Query query = new Query(Criteria.where("foodStallId").is(foodStallId).andOperator(Criteria.where("notificationStatus").is("ACTIVE")));
		
		List<MessageNotification> notifications = mongoTemplate.find(query, MessageNotification.class);
		
		return notifications;
	}
	
	public List<CustomerNotification> getPendingCustomerNotifications(String customerPhoneNumber){
		
		Query query = new Query(Criteria.where("customerPhoneNumber").is(customerPhoneNumber).andOperator(Criteria.where("notificationStatus").is("ACTIVE")));
		
		List<CustomerNotification> notifications = mongoTemplate.find(query, CustomerNotification.class);
		
		return notifications;
	}
	
	public void markNotificationAsRead(String notificationId) {
		Query query = new Query(Criteria.where("_id").is(notificationId));
		
		System.out.println("In Repository...");
		
		MessageNotification notification = mongoTemplate.findOne(query, MessageNotification.class);
		
		System.out.println("In Repository..." + notification);
		
		notification.setNotificationStatus("READ");
		
		mongoTemplate.save(notification);
	}
	
	public void markCustomerNotificationAsRead(String notificationId) {
		Query query = new Query(Criteria.where("_id").is(notificationId));
		
		System.out.println("In Repository...");
		
		CustomerNotification notification = mongoTemplate.findOne(query, CustomerNotification.class);
		
		System.out.println("In Repository..." + notification);
		
		notification.setNotificationStatus("READ");
		
		mongoTemplate.save(notification);
	}
	
	
}
