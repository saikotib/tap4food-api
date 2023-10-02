package com.endeavour.tap4food.user.app.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.endeavour.tap4food.app.response.dto.PaytmTransactionsResponse;
import com.endeavour.tap4food.user.app.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pg.merchant.PaytmChecksum;

public class PaytmService {
	
	@Autowired
	private OrderRepository orderRepository;
	
	public String getTransactionToken(String orderID,String customerID, String txnAmountinput) {
		
		  try {
	            JSONObject paytmParams = new JSONObject();
	            JSONObject body = new JSONObject();
	            body.put("requestType", "Payment");
	            body.put("mid", "ykrjMy07609170067260");
	            body.put("websiteName", "DEFAULT");
	            body.put("orderId", orderID);
	            body.put("redirect", false);
//	            body.put("callbackUrl", "https://dev.tap4food.com/tf/api/customer/payments/pgresponse");
	            body.put("callbackUrl", "https://tap4food.com/tf/api/customer/payments/pgresponse");
//	            body.put("callbackUrl", "http://localhost/tf/api/customer/payments/pgresponse");
	            JSONObject txnAmount = new JSONObject();
	            txnAmount.put("value", txnAmountinput);
	            txnAmount.put("currency", "INR");

	            JSONObject userInfo = new JSONObject();
	            userInfo.put("custId", customerID);
	            userInfo.put("mobile", customerID);
	            body.put("txnAmount", txnAmount);
	            body.put("userInfo", userInfo);

	            String checksum = PaytmChecksum.generateSignature(body.toString(), "0AOmW1nGtH9MSvgA");

	            JSONObject head = new JSONObject();
	            head.put("signature", checksum);

	            paytmParams.put("body", body);
	            paytmParams.put("head", head);

	            String post_data = paytmParams.toString();

	            // Use the appropriate URL based on your environment (Staging or Production)
//	            URL url = new URL("https://securegw-stage.paytm.in/theia/api/v1/initiateTransaction?mid=YOUR_MID_HERE&orderId=ORDERID_98765");

	            // For Production URL:
	             URL url = new URL("https://securegw.paytm.in/theia/api/v1/initiateTransaction?mid=ykrjMy07609170067260&orderId="+orderID);

	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("POST");
	            connection.setRequestProperty("Content-Type", "application/json");
	            connection.setDoOutput(true);

	            DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
	            requestWriter.writeBytes(post_data);
	            requestWriter.close();

	            String responseData = "";
	            InputStream is = connection.getInputStream();
	            BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
	            if ((responseData = responseReader.readLine()) != null) {
	                System.out.append("Response: " + responseData);
	                return responseData;
	            }
	            responseReader.close();
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }
		return "Token not Generated";
	}
	
	 public static String transactionStatus(String orderId ) {
	        try {
	        	boolean isStaging = false;
	            // Initialize an object to hold the request parameters.
	            JSONObject paytmParams = new JSONObject();

	            // Create a JSON object for the body parameters.
	            JSONObject body = new JSONObject();
	            body.put("mid", "ykrjMy07609170067260");
	            body.put("orderId", orderId);

	            // Generate the checksum using the body parameters and merchant key.
	            String checksum = PaytmChecksum.generateSignature(body.toString(), "0AOmW1nGtH9MSvgA");

	            // Create a JSON object for the head parameters and add the checksum.
	            JSONObject head = new JSONObject();
	            head.put("signature", checksum);

	            // Add body and head to paytmParams.
	            paytmParams.put("body", body);
	            paytmParams.put("head", head);
	            String post_data = paytmParams.toString();

	            // Define the URL for either staging or production environment.
	            String apiUrl = isStaging ? "https://securegw-stage.paytm.in/v3/order/status" : "https://securegw.paytm.in/v3/order/status";
	            URL url = new URL(apiUrl);

	            // Open an HTTP connection.
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("POST");
	            connection.setRequestProperty("Content-Type", "application/json");
	            connection.setDoOutput(true);

	            // Write the JSON request body to the output stream.
	            DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
	            requestWriter.writeBytes(post_data);
	            requestWriter.close();

	            // Read the response from the API.
	            String responseData = "";
	            InputStream is = connection.getInputStream();
	            BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
	            if ((responseData = responseReader.readLine()) != null) {
	                // Return the response as a string.
	            	ObjectMapper object = new ObjectMapper();
	            		
	            	PaytmTransactionsResponse paytmResPonse = object.readValue(responseData, PaytmTransactionsResponse.class);
	            	
	            	
	            
	                return responseData;
	            }
	            responseReader.close();
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }

	        // Return null if there was an error.
	        return null;
	    }

}
