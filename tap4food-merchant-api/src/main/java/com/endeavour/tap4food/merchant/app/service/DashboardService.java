package com.endeavour.tap4food.merchant.app.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.response.dto.MerchantDashboardResponse;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.merchant.app.repository.ManageOrderRepository;

@Service
public class DashboardService {

	@Autowired
	private ManageOrderRepository manageOrderRepository;
	
	public MerchantDashboardResponse getDashboardData(Long foodStallId) {
		
		MerchantDashboardResponse response = new MerchantDashboardResponse();
	
		List<Order> allOrdersList = manageOrderRepository.getOrders(foodStallId);
		
		long allOrders = 0;
		long openOrders = 0;
		long todayOrders = 0;
		long cancelledOrders = 0;
		long deliveredOrders = 0;
		
		String todayDate = DateUtil.getPresentDate();
		
		Map<String, MerchantDashboardResponse.OrderReport> orderStatsMap = new TreeMap<>();
		
		for(Order order : allOrdersList) {
			String status = order.getStatus();
			
			String orderedDate = order.getOrderedTime();
			String orderMonth = String.valueOf(DateUtil.getMonth(orderedDate));
			
			if(!orderStatsMap.containsKey(orderMonth)) {
				
				orderStatsMap.put(orderMonth, new MerchantDashboardResponse.OrderReport());
			}
			
			MerchantDashboardResponse.OrderReport stat = orderStatsMap.get(orderMonth);
			
			if(status.equalsIgnoreCase("DELIVERED")) {
				deliveredOrders ++;
				stat.setDeliveredOrders(stat.getDeliveredOrders() + 1);
			}else if(status.equalsIgnoreCase("CANCELLED")) {
				cancelledOrders ++;
				stat.setCancelledOrders(stat.getCancelledOrders() + 1);
			}else {
				openOrders ++;
			}
			
			stat.setRecievedOrders(stat.getRecievedOrders() + 1);
			
			if(orderedDate.indexOf(todayDate) > -1) {
				todayOrders ++;
			}		
			
			allOrders ++;
		}
		
		response.setAllOrdersList(allOrdersList);
		
		response.setOpenOrders(openOrders);
		response.setDeliveredOrders(deliveredOrders);
		response.setCancelledOrders(cancelledOrders);
		response.setAllOrders(allOrders);
		response.setTodayOrders(todayOrders);
		response.setSubscription("Free Trial");
		response.setOrderStatistics(this.enrichMonthlyStats(orderStatsMap));
		
		System.out.println(response);
		
		return response;
	}
	
	private Map<String, MerchantDashboardResponse.OrderReport> enrichMonthlyStats(Map<String, MerchantDashboardResponse.OrderReport> orderStatsMap) {
		
		Map<String, MerchantDashboardResponse.OrderReport> response = new LinkedHashMap<String, MerchantDashboardResponse.OrderReport>();
		
		if(!orderStatsMap.containsKey("1")) {
			response.put("JAN", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("JAN", orderStatsMap.get("1"));
		}
		
		if(!orderStatsMap.containsKey("2")) {
			response.put("FEB", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("FEB", orderStatsMap.get("2"));
		}
		
		if(!orderStatsMap.containsKey("3")) {
			response.put("MAR", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("MAR", orderStatsMap.get("3"));
		}
		
		if(!orderStatsMap.containsKey("4")) {
			response.put("APR", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("APR", orderStatsMap.get("4"));
		}
		
		if(!orderStatsMap.containsKey("5")) {
			response.put("MAY", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("MAY", orderStatsMap.get("5"));
		}
		
		if(!orderStatsMap.containsKey("6")) {
			response.put("JUN", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("JUN", orderStatsMap.get("6"));
		}
		
		if(!orderStatsMap.containsKey("7")) {
			response.put("JUL", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("JUL", orderStatsMap.get("7"));
		}
		
		if(!orderStatsMap.containsKey("8")) {
			response.put("AUG", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("AUG", orderStatsMap.get("8"));
		}
		
		if(!orderStatsMap.containsKey("9")) {
			response.put("SEP", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("SEP", orderStatsMap.get("9"));
		}
		
		if(!orderStatsMap.containsKey("10")) {
			response.put("OCT", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("OCT", orderStatsMap.get("10"));
		}
		
		if(!orderStatsMap.containsKey("11")) {
			response.put("NOV", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("NOV", orderStatsMap.get("11"));
		}
		
		if(!orderStatsMap.containsKey("12")) {
			response.put("DEC", new MerchantDashboardResponse.OrderReport());
		}else {
			response.put("DEC", orderStatsMap.get("12"));
		}
		
		return response;
	}
}
