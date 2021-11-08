package com.endeavour.tap4food.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.response.dto.OrderDto;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.ManageOrderService;

@RestController
@RequestMapping("/api/merchant/orders")
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
}