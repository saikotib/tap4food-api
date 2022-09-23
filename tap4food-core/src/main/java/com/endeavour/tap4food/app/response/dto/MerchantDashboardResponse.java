package com.endeavour.tap4food.app.response.dto;

import java.util.Map;

import lombok.Data;

@Data
public class MerchantDashboardResponse {

	private long openOrders;
	
	private long deliveredOrders;
	
	private long cancelledOrders;
		
	private long allOrders;
	
	private long todayOrders;
	
	private String subscription;
	
	private Map<String, OrderReport> orderStatistics;		// Key is month here
	
	@Data
	public static class OrderReport{
		
		private long deliveredOrders;
		
		private long cancelledOrders;
		
		private long recievedOrders;
	}
}
