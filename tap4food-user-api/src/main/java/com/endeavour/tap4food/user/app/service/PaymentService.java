package com.endeavour.tap4food.user.app.service;

import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.order.RazorPayOrder;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Transfer;

@Service
public class PaymentService {

//	Test Keys
//	private static final String SECRET_ID = "rzp_test_rMdZ1T7Z6asYXb";
//	private static final String SECRET_KEY = "ukwYXWOOjITBxaRZT8fWYt2C";
	
//	Live Details	
	private static final String SECRET_ID = "rzp_live_ozqtNqhs4Il6ST";
	private static final String SECRET_KEY = "NbAPtMKFO0Zyal6QpgG8Da7W";

	@Bean
	private RazorpayClient getClient() throws RazorpayException {
		RazorpayClient client = new RazorpayClient(SECRET_ID, SECRET_KEY); 
		
		return client;
	}
	
	public RazorPayOrder createRPOrder(String customerPhoneNumber, Double amount) throws RazorpayException {
		
		RazorPayOrder rzpOrder = new RazorPayOrder();
		
		int orderAmount = (int) Math.round(amount * 100);
		
		JSONObject options = new JSONObject();
		options.put("amount", String.valueOf(orderAmount));
		options.put("currency", "INR");
		options.put("receipt", customerPhoneNumber);
		options.put("payment_capture", 1); // You can enable this if you want to do Auto Capture. 
		Order order = getClient().Orders.create(options);
		
		System.out.println(order);
		
		rzpOrder.setOrderId(order.get("id"));
		rzpOrder.setAmount(order.get("amount"));
		rzpOrder.setAmountDue(order.get("amount_due"));
		rzpOrder.setCreatedTimeMs(order.get("created_at"));
		rzpOrder.setCurrency(order.get("currency"));
		rzpOrder.setReciept(order.get("receipt"));
		
		return rzpOrder;
	}
	
	public Transfer directTransfer(String account, Double amount) throws RazorpayException {
		
		int orderAmount = (int) Math.round(amount * 100);
		
		JSONObject request = new JSONObject();
		request.put("amount", orderAmount);
		request.put("currency", "INR");
		request.put("account", account);
		Transfer transfer = getClient().Transfers.create(request);
		
		return transfer;
	}
}
