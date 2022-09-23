package com.endeavour.tap4food.merchant.app.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.MerchantSettings;
import com.endeavour.tap4food.app.model.notifications.CustomerNotification;
import com.endeavour.tap4food.app.model.notifications.Notification;
import com.endeavour.tap4food.merchant.app.repository.FoodStallRepository;
import com.endeavour.tap4food.merchant.app.repository.MerchantRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationClientService {

	@Value("${api.base.url}")
	private String API_BASE_URL;
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	
	@Autowired
	private MerchantRepository merchantRepository;
	
	@Autowired
	private FoodStallRepository foodStallRepository;
	
	private static final String MERCHANT_WS_MESSAGE_TRANSFER_DESTINATION = "/user/%s/private";

	public void sendMessageToCustomer(CustomerNotification notification, String phoneNumber) {
		
		RestTemplate restTemplate = new RestTemplate();
		
		String notificationEndpoint = API_BASE_URL + "/api/customer/notifications/sendNotificationToCustomer";
		
		System.out.println("notificationEndpoint = " + notificationEndpoint);
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<CustomerNotification> request = new HttpEntity<CustomerNotification>(notification, httpHeaders);
		
		String response = 
			      restTemplate.postForObject(notificationEndpoint, request, String.class);
		
		System.out.println("response = " + response);
    }
	
	public void sendMessageToMerchantGUI(Notification notification) {
		
		log.info("Inside the sendMessageToMerchant()");
		log.info("Notification : {}", notification.toString());
		
		FoodStall stall = foodStallRepository.getFoodStallById(Long.valueOf(notification.getReciever()));
		
		MerchantSettings settings = merchantRepository.getSettings(stall.getMerchantId());
		
		if(Objects.nonNull(settings) && settings.isOrderNotifications()) {
			String topic = String.format(MERCHANT_WS_MESSAGE_TRANSFER_DESTINATION, notification.getReciever());
			
	        simpMessagingTemplate.convertAndSend(topic, notification);
		}else {
			log.info("Order notifications are disabled. Hence not processing the notifications.");
		}
    }
}
