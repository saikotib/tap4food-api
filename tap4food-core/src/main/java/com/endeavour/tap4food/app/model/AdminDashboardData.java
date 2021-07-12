package com.endeavour.tap4food.app.model;

import java.util.Map;

import lombok.Data;

@Data
public class AdminDashboardData {

	private Long shoppingMalls;

	private Long theaters;

	private Long restaurants;

	private Long totalOrders;

	private Long totalFoodStalls;

	private Long totalFoodCourts;

	private Long totalMerchants;

	private Long totalCustomers;

	private Map<String, Map<String, ReportParams>> reportMap;
	
	private Map<String, Subscriptions> subscriptionsMap;
	
	private Map<String, MerchantRequests> merchantRequestsMap;
	
	private Map<String, Cuisines> cuisinesMap;
	
	private Map<String, MerchantVsRevenue> merchantVsRevenueMap;

	@Data
	public static class ReportParams {

		private Long foodStalls;

		private Long restaurants;

		private Long customers;
	}

	@Data
	public static class Subscriptions {

		private Long newSubscriptions;

		private Long expired;

		private Long renewal;
	}
	
	@Data
	public static class MerchantRequests {

		private Long open;

		private Long inProgress;

		private Long rejected;
		
		private Long approved;
	}
	
	@Data
	public static class Cuisines {

		private Long northIndian;

		private Long southIndian;

		private Long streetFood;
		
		private Long italianCuisine;
		
		private Long mexicanCuisine;
	}
	
	@Data
	public static class MerchantVsRevenue {

		private Double merchants;

		private Double revenue;
	}
}
