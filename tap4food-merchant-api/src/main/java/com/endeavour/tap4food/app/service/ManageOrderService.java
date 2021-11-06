package com.endeavour.tap4food.app.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.enums.OrderStatusEnum;
import com.endeavour.tap4food.app.model.order.Customer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.repository.ManageOrderRepository;
import com.endeavour.tap4food.app.response.dto.OrderDto;

@Service
public class ManageOrderService {

	@Autowired
	private ManageOrderRepository manageOrderRepository;
	
	public List<OrderDto> getOrders() {
		List<OrderDto> orders = new ArrayList<OrderDto>();
		
		List<Order> allOrders = manageOrderRepository.getOrders();
		
		for(Order order : allOrders) {
			OrderDto orderDto = new OrderDto();
			
			orderDto.setOrderId(order.getOrderId());
			orderDto.setOrderNumber(order.getId());
			orderDto.setFoodStallId(order.getFoodStallId());
			orderDto.setSelfPickup(order.isSelfPickup());
			orderDto.setStatus(order.getStatus());
			orderDto.setTotalAmount(order.getGrandTotal());
			
			Customer customer = manageOrderRepository.getOrderCustomer(order.getOrderId());
			
			orderDto.setCustomerName(customer.getFullName());
			orderDto.setCustomerPhoneNumber(customer.getPhoneNumber());
			
			if(!order.isSelfPickup()) {
				orderDto.setSeatNumber(order.getSeatNumber());
				if(order.isTheatre()) {
					orderDto.setScreen(order.getScreenNumber());
				}else {
					orderDto.setTableNumber(order.getSeatNumber());
				}
			}
		}
		
		return orders;
	}
}
