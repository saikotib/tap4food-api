package com.endeavour.tap4food.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.request.dto.PlaceOrderRequest;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.OrderService;

@RestController
@RequestMapping("/api/customer")
public class OrderController {
	
	@Autowired
	private OrderService orderService;

	@RequestMapping(value = "/placeOrder", method = RequestMethod.POST ,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> placeOrder(@RequestBody PlaceOrderRequest orderRequest){
		
		Order order = orderService.placeOrder(orderRequest);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("OK")
				.data(order)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
}
