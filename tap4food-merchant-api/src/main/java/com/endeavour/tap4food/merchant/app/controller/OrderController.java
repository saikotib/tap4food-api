package com.endeavour.tap4food.merchant.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.response.dto.OrderDto;
import com.endeavour.tap4food.app.response.dto.OrderFeedbackDto;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.merchant.app.service.ManageOrderService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/merchant/orders")
@Api(tags = "OrderController", description = "OrderController")
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
	public ResponseEntity<ResponseHolder> getOrderHistory(@RequestParam("fsId") Long foodStallId){
		
		List<OrderDto> orders = manageOrderService.getOrderHistory(foodStallId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(orders)
				.build();
		
		return ResponseEntity.ok().body(response);		
	}
	
	@RequestMapping(value = "/update-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateStatus(@RequestParam("orderId") Long orderId, @RequestParam("status") String status) throws TFException{
		
		manageOrderService.updateOrderStatus(orderId, status);
		
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
}