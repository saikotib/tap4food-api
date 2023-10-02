package com.endeavour.tap4food.user.app.service;

import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
        int maxAttempts = 3; // Maximum number of retry attempts
        int currentAttempt = 0;
        long retryDelayMillis = 1000; // 1 second (adjust as needed)

        while (currentAttempt < maxAttempts) {
            try {
                // Create a custom TrustManager that trusts all certificates
                TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
                };

                // Create an SSLContext with the custom TrustManager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

                // Create a CloseableHttpClient with the custom SSLContext
                CloseableHttpClient httpClient = HttpClients.custom()
                        .setSslcontext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // Allow all hostnames
                        .build();

                // Create a RestTemplate with the custom HttpClient
                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
                RestTemplate restTemplate = new RestTemplate(factory);

                String notificationEndpoint = API_BASE_URL + "/api/merchant/notifications/sendNotificationToMerchant";
                System.out.println("notificationEndpoint = " + notificationEndpoint);

                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Notification> request = new HttpEntity<>(notification, httpHeaders);

                ResponseEntity<String> response =
                        restTemplate.exchange(notificationEndpoint, HttpMethod.POST, request, String.class);

                System.out.println("response = " + response);

                // If the code reaches this point without throwing an exception, the operation was successful.
                break; // Exit the loop since we don't need to retry.
            } catch (Exception e) {
                e.printStackTrace();
                currentAttempt++;

                if (currentAttempt < maxAttempts) {
                    // If the maximum number of attempts is not reached, introduce a delay before retrying.
                    try {
                        TimeUnit.MILLISECONDS.sleep(retryDelayMillis); // Sleep before retrying
                    } catch (InterruptedException sleepException) {
                        Thread.currentThread().interrupt(); // Restore the interrupted status.
                    }
                } else {
                    // If the maximum number of attempts is reached, log an error message or take appropriate action.
                    System.out.println("Maximum retry attempts reached. Giving up.");
                    break; // Exit the loop.
                }
            }
        }
    }

}
