package com.endeavour.tap4food.user.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.model.order.UpdatePaymentDetailsRequest;
import com.endeavour.tap4food.app.request.dto.PlaceOrderRequest;
import com.endeavour.tap4food.app.response.dto.OrderDto;
import com.endeavour.tap4food.user.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.user.app.service.OrderService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/customer")
@Api(tags = "OrderController", description = "OrderController")
@CrossOrigin
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
//	@Autowired
//	private PaymentService paymentService;


	@RequestMapping(value = "/placeOrder", method = RequestMethod.POST ,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> placeOrder(@RequestBody PlaceOrderRequest orderRequest){
		
		Order order = orderService.placeOrder(orderRequest);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("OK")
				.data(order)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/updatePaymentStatus", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updatePaymentStatus(@RequestBody UpdatePaymentDetailsRequest request){
		
//		Order order = orderService.updateOrderPaymentStatus(request);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("OK")
				.data("Disabled")
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	/*
	@RequestMapping(value = "/testPayment", method = RequestMethod.POST ,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> testPayment() throws RazorpayException{
		
		paymentService.createRPOrder("123456", 100.0);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("OK")
				.data("Testing is done")
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	*/
	@RequestMapping(value = "/getOrders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOrders(@RequestParam("phoneNumber") String phoneNunber){
		
		List<OrderDto> orders = orderService.getOrders(phoneNunber);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("OK")
				.data(orders)
				.build();
		
		return ResponseEntity.ok().body(response);
	} 
	
}
