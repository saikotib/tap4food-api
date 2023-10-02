package com.endeavour.tap4food.merchant.app.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.response.dto.OrderDto;
import com.endeavour.tap4food.app.response.dto.OrderFeedbackDto;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.merchant.app.payload.response.PaytmTransactionResponse;
import com.endeavour.tap4food.merchant.app.service.ManageOrderService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/merchant/orders")
@Api(tags = "OrderController", description = "OrderController")
@CrossOrigin
public class OrderController {

	@Autowired
	private ManageOrderService manageOrderService;
	
	@RequestMapping(value = "/get-orders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOrders(@RequestParam("fsId") Long foodStallId){
		
		Map<String, List<OrderDto>> orders = manageOrderService.getOrders(foodStallId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(orders)
				.build();
		
		return ResponseEntity.ok().body(response);		
	}
	
	@RequestMapping(value = "/get-order-history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOrderHistory(@RequestParam("fsId") Long foodStallId) throws Exception{
		
		LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = today.format(formatter);

        Thread thread = new Thread(() -> {
            try {
				updateTransactionDetails(formattedDate);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });

        thread.start();

        // Continue with other main thread logic

        
        
		
		List<OrderDto> orders = manageOrderService.getOrderHistory(foodStallId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(orders)
				.build();
		
		return ResponseEntity.ok().body(response);		
	}
	
	@RequestMapping(value = "/update-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateStatus(@RequestParam("orderId") Long orderId, 
			@RequestParam("status") String status,
			@RequestParam(name = "deliveryTime", required = false) String deliveryTime) throws TFException{
		
		manageOrderService.updateOrderStatus(orderId, status, deliveryTime);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data("Status updated")
				.build();
		
		return ResponseEntity.ok().body(response);		
	}
		
	@RequestMapping(value = "/get-notifications", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getNotifications(@RequestParam("fsId") Long foodStallId){
		
		List<MessageNotification> notifications = manageOrderService.getNotifications(foodStallId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(notifications)
				.build();
		
		return ResponseEntity.ok().body(response);		
	}
	
	@RequestMapping(value ="/update-transactiondetails" ,method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateTransactionDetails(@RequestParam("date") String date) throws Exception {
		
		return manageOrderService.updatepaytmPaymentdetails(date);
	}
	@RequestMapping(value = "/get-orders-feedback", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOrdersFeedback(@RequestParam("fsId") Long foodStallId){
		
		List<OrderFeedbackDto> feedbackList = manageOrderService.getOrderFeedbacks(foodStallId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(feedbackList)
				.build();
		
		return ResponseEntity.ok().body(response);		
	}
	
	@RequestMapping(value = "/get-orders-complaints", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOrdersComplaints(@RequestParam("fsId") Long foodStallId){
		
		List<OrderFeedbackDto> feedbackList = manageOrderService.getOrderComplaints(foodStallId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(feedbackList)
				.build();
		
		return ResponseEntity.ok().body(response);		
	}
	
	@RequestMapping(value = "/order-transaction-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOrderTransancationDetails(@RequestParam("orderId") Long orderId) throws TFException{
		
		PaytmTransactionResponse transactionStatus = manageOrderService.transactionStatus(orderId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(transactionStatus)
				.build();
		
		return ResponseEntity.ok().body(response);		
	}
	
}