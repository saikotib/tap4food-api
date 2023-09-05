package com.endeavour.tap4food.app.response.dto;

import java.util.List;
import java.util.Map;

import com.endeavour.tap4food.app.model.order.Order;

import lombok.Data;

@Data
public class MerchantDashboardResponse {

	private long openOrders;
	
	private long deliveredOrders;
	
	private long cancelledOrders;
		
	private long allOrders;
	
	private long todayOrders;
	
	private String subscription;
	
	private List<Order> allOrdersList;
	
	private Map<String, OrderReport> orderStatistics;		// Key is month here
	
	@Data
	public static class OrderReport{
		
		private long deliveredOrders;
		
		private long cancelledOrders;
		
		private long recievedOrders;
	}
}
