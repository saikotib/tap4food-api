package com.endeavour.tap4food.user.app.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.user.app.config.PaytmDetailPojo;
import com.endeavour.tap4food.user.app.service.OrderService;
import com.paytm.pg.merchant.PaytmChecksum;

@Controller
@RequestMapping("/api/customer/payments")
public class PaymentController {

	@Autowired
	private PaytmDetailPojo paytmDetailPojo;

	@Autowired
	private OrderService orderService;

	@GetMapping("/home")
	public String home() {
		return "home";
	}

	@PostMapping(value = "/submitPaymentDetail")
	public ModelAndView getRedirect(@RequestParam("MOBILE_NO") String mobileNumber, @RequestParam("EMAIL") String email,
			@RequestParam("ORDER_ID") String orderId, @RequestParam("TXN_AMOUNT") String txnAmount) throws Exception {

		orderId = orderId.replaceAll(",", "");
		txnAmount = txnAmount.replaceAll(",", "");
		System.out.println("mobileNumber :" + mobileNumber);
		System.out.println("orderId :" + orderId);
		System.out.println("email :" + email);
		System.out.println("txnAmount :" + txnAmount);

		System.out.println("inside submitPaymentDetail");
		ModelAndView modelAndView = new ModelAndView("redirect:" + paytmDetailPojo.getPaytmUrl());
		TreeMap<String, String> parameters = new TreeMap<>();
		paytmDetailPojo.getDetails().forEach((k, v) -> parameters.put(k, v));
		parameters.put("MOBILE_NO", mobileNumber);
		parameters.put("EMAIL", email);
		parameters.put("ORDER_ID", orderId);
		parameters.put("TXN_AMOUNT", txnAmount);
		parameters.put("CUST_ID", mobileNumber);
		String checksum = getCheckSum(parameters);
		parameters.put("CHECKSUMHASH", checksum);
		modelAndView.addAllObjects(parameters);
		return modelAndView;
	}

	@PostMapping(value = "/pgresponse")
	public String getResponseRedirect(HttpServletRequest request, Model model) {

		System.out.println("Inside pgresponse");
		Map<String, String[]> mapData = request.getParameterMap();
		System.out.println("mapData :" + mapData);
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		String paytmChecksum = "";
		for (Entry<String, String[]> requestParamsEntry : mapData.entrySet()) {
			System.out.println("KEY : " + requestParamsEntry.getKey() + " & VAL: " + requestParamsEntry.getValue()[0]);
			if ("CHECKSUMHASH".equalsIgnoreCase(requestParamsEntry.getKey())) {
				paytmChecksum = requestParamsEntry.getValue()[0];
			} else {
				parameters.put(requestParamsEntry.getKey(), requestParamsEntry.getValue()[0]);
			}
		}
		String result;

		boolean isValideChecksum = false;
		System.out.println("RESULT : " + parameters.toString());
		try {
			isValideChecksum = validateCheckSum(parameters, paytmChecksum);
			if (isValideChecksum && parameters.containsKey("RESPCODE")) {
				if (parameters.get("RESPCODE").equals("01")) {
					result = "Payment Successful";
				} else {
					result = "Payment Failed";
				}
			} else {
				result = "Checksum mismatched";
			}
		} catch (Exception e) {
			result = e.toString();
		}
		model.addAttribute("result", result);
		parameters.remove("CHECKSUMHASH");
		Map<String, String> paymentParamsMap = new LinkedHashMap<String, String>();
		paymentParamsMap.put("Order ID", parameters.get("ORDERID"));
		paymentParamsMap.put("TransactionID", parameters.get("TXNID"));
		paymentParamsMap.put("Transaction Date", parameters.get("TXNDATE"));
		paymentParamsMap.put("Transaction Amount", parameters.get("TXNAMOUNT"));
		paymentParamsMap.put("Payment Status", parameters.get("RESPMSG"));
		String status = "S";
		String paymentStatus = "Completed";
		String statusMsg = "Order is placed";
		if (!"TXN_SUCCESS".equalsIgnoreCase(parameters.get("STATUS"))) {
			status = "F";
			paymentStatus = "Failed";
			statusMsg = "Couldn't place your order";
		}

		Order order = orderService.getOrder(Long.parseLong(parameters.get("ORDERID")));

		orderService.updateOrderPaymentStatus(parameters, paymentStatus, Long.parseLong(parameters.get("ORDERID")));
		String url = "";
		if (!"TXN_SUCCESS".equalsIgnoreCase(parameters.get("STATUS"))) {

//			url = String.format("https://user.tap4food.com/customer/cart?orderId=%s&status=%s&stallId=%s",
//					parameters.get("ORDERID"), status, order.getFoodStallId());

//			url = String.format("http://localhost:3000/customer/cart?orderId=%s&status=%s&stallId=%s",
//					parameters.get("ORDERID"), status, order.getFoodStallId());
//			url = String.format("https://user.tap4food.com/customer/payment_response?orderId=%s&status=%s&stallId=%s",parameters.get("ORDERID"), status, order.getFoodStallId());
			url = String.format("https://user.dev.tap4food.com/customer/payment_response?orderId=%s&status=%s&stallId=%s",parameters.get("ORDERID"), status, order.getFoodStallId());
			
			
		} else {
//			url = String.format("https://user.tap4food.com/customer/payment_response?orderId=%s&status=%s&stallId=%s",parameters.get("ORDERID"), status, order.getFoodStallId());
			url = String.format("https://user.dev.tap4food.com/customer/payment_response?orderId=%s&status=%s&stallId=%s",parameters.get("ORDERID"), status, order.getFoodStallId());
//			url = String.format("http://localhost:3000/customer/payment_response?orderId=%s&status=%s&stallId=%s",parameters.get("ORDERID"), status, order.getFoodStallId());

//			url = String.format("https://user.tap4food.com/customer/home");

			
		}

		model.addAttribute("parameters", paymentParamsMap);
		model.addAttribute("redirectUrl", url);
		model.addAttribute("statusMsg", statusMsg);

		return "report";
	}

	private String getCheckSum(TreeMap<String, String> parameters) throws Exception {
		return PaytmChecksum.generateSignature(parameters, paytmDetailPojo.getMerchantKey());
	}

	private boolean validateCheckSum(TreeMap<String, String> parameters, String paytmChecksum) throws Exception {
		return PaytmChecksum.verifySignature(parameters, paytmDetailPojo.getMerchantKey(), paytmChecksum);
	}
}
