package com.endeavour.tap4food.merchant.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.response.dto.MerchantDashboardResponse;
import com.endeavour.tap4food.merchant.app.repository.ManageOrderRepository;

@Service
public class DashboardService {

	@Autowired
	private ManageOrderRepository manageOrderRepository;
	
	public MerchantDashboardResponse getDashboardData(Long foodStallId) {
		
		MerchantDashboardResponse response = new MerchantDashboardResponse();
	
		List<Order> allOrdersList = manageOrderRepository.getOrders(foodStallId);
		
		long allOrders = 0;
		long newOrders = 0;
		long inPreparation = 0;
		long readyOrders = 0;
		long cancelledOrders = 0;
		long deliveredOrders = 0;
		
		for(Order order : allOrdersList) {
			String status = order.getStatus();
			
			if(status.equalsIgnoreCase("NEW")) {
				newOrders ++;
			}else if(status.equalsIgnoreCase("DELIVERED")) {
				deliveredOrders ++;
			}
		}
		
		response.setNewOrders(newOrders);
		response.setDeliveredOrders(deliveredOrders);
		
		return response;
	}
}
