package com.endeavour.tap4food.user.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.endeavour.tap4food.app.model.notifications.CustomerNotification;
import com.endeavour.tap4food.app.model.notifications.Notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebSocketNotificationService {
	
	@Value("${api.base.url}")
	private String API_BASE_URL;
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	
	private static final String USER_WS_MESSAGE_TRANSFER_DESTINATION = "/user/%s/private";
	
	public void sendMessageToCustomerGUI(CustomerNotification notification, String phoneNumber) {
		
		log.info("Inside the sendMessageToCustomer()");
		log.info("CustomerNotification : {}", notification.toString());
		
		String topic = String.format(USER_WS_MESSAGE_TRANSFER_DESTINATION, phoneNumber);
		
        simpMessagingTemplate.convertAndSend(topic,
        		notification);
    }
	
	public void sendNotificationToMerchant(Notification notification) {
		
		RestTemplate restTemplate = new RestTemplate();
		
		String notificationEndpoint = API_BASE_URL + "/api/merchant/notifications/sendNotificationToMerchant";
		
		System.out.println("notificationEndpoint = " + notificationEndpoint);
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<Notification> request = new HttpEntity<Notification>(notification, httpHeaders);
		
		String response = 
			      restTemplate.postForObject(notificationEndpoint, request, String.class);
		
		System.out.println("response = " + response);
		
	}
}
