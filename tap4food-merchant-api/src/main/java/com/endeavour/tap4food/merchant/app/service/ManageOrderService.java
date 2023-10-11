package com.endeavour.tap4food.merchant.app.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.CustomerComplaints;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.OrderFeedback;
import com.endeavour.tap4food.app.model.notifications.CustomerNotification;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.model.order.CartItem;
import com.endeavour.tap4food.app.model.order.CartItemCustomization;
import com.endeavour.tap4food.app.model.order.Customer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.model.order.OrderedOfferItems;
import com.endeavour.tap4food.app.response.dto.OrderDto;
import com.endeavour.tap4food.app.response.dto.OrderFeedbackDto;
import com.endeavour.tap4food.app.response.dto.PaytmTransactionsResponse;
import com.endeavour.tap4food.app.response.dto.SettlementListResponse;
import com.endeavour.tap4food.app.response.dto.SettlementTransaction;
import com.endeavour.tap4food.app.service.NotificationService;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.merchant.app.payload.response.PaytmTransactionResponse;
import com.endeavour.tap4food.merchant.app.repository.ManageOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pg.merchant.PaytmChecksum;

@Service
public class ManageOrderService {

	@Autowired
	private ManageOrderRepository manageOrderRepository;

	@Autowired
	private FoodStallService foodStallService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationClientService notificationClientService;

	public Map<String, List<OrderDto>> getOrders(Long foodStallId) {

		Map<String, List<OrderDto>> ordersMap = new HashMap<String, List<OrderDto>>();

		List<OrderDto> orders = new ArrayList<OrderDto>();

		List<Order> allOrders = manageOrderRepository.getOrders(foodStallId);

		for (Order order : allOrders) {

			if (order.getStatus().equalsIgnoreCase("DELIVERED") && !DateUtil.isToday(order.getOrderedTime())) {
				continue;
			}

			OrderDto orderDto = new OrderDto();

			orderDto.setOrderId(order.getOrderId());
			orderDto.setOrderNumber(order.getId());
			orderDto.setFoodStallId(order.getFoodStallId());
			orderDto.setSelfPickup(order.isSelfPickup());
			orderDto.setStatus(order.getStatus());
			orderDto.setTotalAmount(order.getGrandTotal());
			orderDto.setTotalItems(order.getTotalItems());
			orderDto.setSelfPickup(order.isSelfPickup());
			orderDto.setOtpVerified(order.isOtpVerified());
			orderDto.setOrderedTime(order.getOrderedTime());
			orderDto.setOtp(order.getOtp());
			if(order.getPackagingPrice() != null) {
			orderDto.setPackagingPrice(order.getPackagingPrice());
			}else {
				
				orderDto.setPackagingPrice(Double.valueOf(0.0));
			}
			if (Objects.isNull(order.getTax())) {
				orderDto.setTax(Double.valueOf(5));
			} else {
				orderDto.setTax(order.getTax());
			}

			FoodStall foodStall = foodStallService.getFoodStallById(foodStallId);

			orderDto.setFoodStallName(foodStall.getFoodStallName());

			Customer customer = manageOrderRepository.getOrderCustomer(order.getOrderId());

			orderDto.setCustomerName(customer.getFullName());
			orderDto.setCustomerPhoneNumber(customer.getPhoneNumber());

			if (!order.isSelfPickup()) {
				orderDto.setSeatNumber(order.getSeatNumber());
				if (order.isTheatre()) {
					orderDto.setScreen(order.getScreenNumber());
				} else {
					orderDto.setTableNumber(order.getSeatNumber());
				}
			}

			List<CartItem> cartItems = this.getOrderedItems(order.getOrderId());

			List<OrderDto.OrderedItem> orderedItems = new ArrayList<OrderDto.OrderedItem>();

			for (CartItem item : cartItems) {

//				System.out.println("Cart Item : " + item);

				OrderDto.OrderedItem orderedItem = new OrderDto.OrderedItem();

				orderedItem.setItemId(item.getFoodItemId());
				orderedItem.setItemName(item.getItemName());
				orderedItem.setPrice(item.getFinalPrice());
				orderedItem.setQuantity(item.getQuantity());
				orderedItem.setCustomizationFlag(item.isCustomizationFlag());

				boolean isOffer = item.isOffer();

				if (isOffer) {
					List<OrderedOfferItems> orderedOfferItems = manageOrderRepository
							.getOrderOfferItems(item.getCartItemId());

					Map<String, List<String>> itemsMap = new HashMap<String, List<String>>();

					List<OrderDto.CustomizationItem> customizationsList = new ArrayList<OrderDto.CustomizationItem>();

					for (OrderedOfferItems orderedOfferItem : orderedOfferItems) {
						if (!itemsMap.containsKey(orderedOfferItem.getFoodItemName())) {
							itemsMap.put(orderedOfferItem.getFoodItemName(), new ArrayList<String>());
						}

						itemsMap.get(orderedOfferItem.getFoodItemName()).add(orderedOfferItem.getCustomizationItem());
					}

					for (Map.Entry<String, List<String>> entry : itemsMap.entrySet()) {
						OrderDto.CustomizationItem customizationItemDto = new OrderDto.CustomizationItem();

						customizationItemDto.setName(entry.getKey());

						customizationItemDto.setItems(entry.getValue());

						customizationsList.add(customizationItemDto);
					}

					orderedItem.setCustomizations(customizationsList);
				} else {
					if (item.isCustomizationFlag()) {
						List<OrderDto.CustomizationItem> customizationsList = new ArrayList<OrderDto.CustomizationItem>();
						List<CartItemCustomization> customizations = manageOrderRepository
								.getOrderItemCustomizations(item.getCartItemId());

						Map<String, List<String>> customizationsMap = new HashMap<String, List<String>>();

						for (CartItemCustomization customization : customizations) {
							if (!customizationsMap.containsKey(customization.getCustomizationName())) {
								customizationsMap.put(customization.getCustomizationName(), new ArrayList<String>());
							}

//							System.out.println("Cart Item customization: " + customization);

							String items = customization.getCustomizationItem();

							List<String> itemsList = new ArrayList<String>();

//							System.out.println("Items : " + items);

							if (Objects.isNull(items)) {
								continue;
							}

							if (items.indexOf("###") > -1) {
								String itemTokens[] = items.split("###");

								for (String token : itemTokens) {
									itemsList.add(token);
								}
							} else {
								itemsList.add(items);
							}

							customizationsMap.get(customization.getCustomizationName()).addAll(itemsList);
						}

						for (Map.Entry<String, List<String>> entry : customizationsMap.entrySet()) {
							OrderDto.CustomizationItem customizationItemDto = new OrderDto.CustomizationItem();

							customizationItemDto.setName(entry.getKey());

							customizationItemDto.setItems(entry.getValue());

							customizationsList.add(customizationItemDto);
						}

						orderedItem.setCustomizations(customizationsList);
					} else {
						orderedItem.setCustomizations(Collections.emptyList());
					}
				}

				orderedItems.add(orderedItem);
			}

			orderDto.setOrderedItems(orderedItems);

			if (!ordersMap.containsKey(orderDto.getStatus())) {
				ordersMap.put(orderDto.getStatus(), new ArrayList<OrderDto>());
			}

			ordersMap.get(orderDto.getStatus()).add(orderDto);

			orders.add(orderDto);
		}

//		List<MessageNotification> notifications = this.getNotifications(foodStallId);
//		
//		for(MessageNotification notification : notifications) {
//			manageOrderRepository.inactivateNotification(foodStallId, notification.getOrderId());
//		}

		return ordersMap;
	}

	public List<OrderDto> getOrderHistory(Long foodStallId) {

		List<OrderDto> orders = new ArrayList<OrderDto>();

		List<Order> allOrders = manageOrderRepository.getOrders(foodStallId);

		System.out.println("Size of orders" + allOrders.size());
		for (Order order : allOrders) {
			OrderDto orderDto = new OrderDto();

			orderDto.setOrderId(order.getOrderId());
			orderDto.setOrderNumber(order.getId());
			orderDto.setFoodStallId(order.getFoodStallId());
			orderDto.setSelfPickup(order.isSelfPickup());
			orderDto.setStatus(order.getStatus());
			orderDto.setTotalAmount(order.getGrandTotal());
			orderDto.setTotalItems(order.getTotalItems());
			orderDto.setSelfPickup(order.isSelfPickup());
			orderDto.setOrderedTime(order.getOrderedTime());
			orderDto.setPaymentId(order.getTransactionId());
			orderDto.setSubTotal(order.getSubTotalAmount());
			if(order.getPackagingPrice() != null) {
				orderDto.setPackagingPrice(order.getPackagingPrice());
				}else {
					
					orderDto.setPackagingPrice(Double.valueOf(0.0));
				}
			double calculatedTax = order.getTax() * 0.01 * order.getSubTotalAmount() + orderDto.getPackagingPrice() * 0.05;

			BigDecimal taxBigDecimal = new BigDecimal(calculatedTax);
			BigDecimal roundedTax = taxBigDecimal.setScale(2, RoundingMode.HALF_UP);

			orderDto.setTax(roundedTax.doubleValue());
			;

			if (order.getPayTmTransactionParameters().get("Commission") != null) {
				Double commission = Double.valueOf(order.getPayTmTransactionParameters().get("Commission"));
				orderDto.setCommission(commission);
			}
			if (Objects.nonNull(order.getPayTmTransactionParameters().get("SettlementAmount"))) {
				orderDto.setSettledAmount(
						Double.valueOf(order.getPayTmTransactionParameters().get("SettlementAmount")));
			}

			orderDto.setPaymentMode(order.getPayTmTransactionParameters().get("PaymentMode"));

			FoodStall foodStall = foodStallService.getFoodStallById(foodStallId);

			orderDto.setFoodStallName(foodStall.getFoodStallName());

			Customer customer = manageOrderRepository.getOrderCustomer(order.getOrderId());

			orderDto.setCustomerName(customer.getFullName());
			orderDto.setCustomerPhoneNumber(customer.getPhoneNumber());

			if (!order.isSelfPickup()) {
				orderDto.setSeatNumber(order.getSeatNumber());
				if (order.isTheatre()) {
					orderDto.setScreen(order.getScreenNumber());
				} else {
					orderDto.setTableNumber(order.getSeatNumber());
				}
			}

			List<CartItem> cartItems = this.getOrderedItems(order.getOrderId());

			List<OrderDto.OrderedItem> orderedItems = new ArrayList<OrderDto.OrderedItem>();

			for (CartItem item : cartItems) {

//				System.out.println("Cart Item : " + item);

				OrderDto.OrderedItem orderedItem = new OrderDto.OrderedItem();

				orderedItem.setItemId(item.getFoodItemId());
				orderedItem.setItemName(item.getItemName());
				orderedItem.setPrice(item.getFinalPrice());
				orderedItem.setQuantity(item.getQuantity());
				orderedItem.setCustomizationFlag(item.isCustomizationFlag());

				if (item.isCustomizationFlag()) {
					List<OrderDto.CustomizationItem> customizationsList = new ArrayList<OrderDto.CustomizationItem>();
					List<CartItemCustomization> customizations = manageOrderRepository
							.getOrderItemCustomizations(item.getCartItemId());

					Map<String, List<String>> customizationsMap = new HashMap<String, List<String>>();

					for (CartItemCustomization customization : customizations) {
						if (!customizationsMap.containsKey(customization.getCustomizationName())) {
							customizationsMap.put(customization.getCustomizationName(), new ArrayList<String>());
						}

//						System.out.println("Cart Item customization: " + customization);

						String items = customization.getCustomizationItem();

						List<String> itemsList = new ArrayList<String>();

//						System.out.println("Items : " + items);

						if (Objects.isNull(items)) {
							continue;
						}

						if (items.indexOf("###") > -1) {
							String itemTokens[] = items.split("###");

							for (String token : itemTokens) {
								itemsList.add(token);
							}
						} else {
							itemsList.add(items);
						}

						customizationsMap.get(customization.getCustomizationName()).addAll(itemsList);
					}

					for (Map.Entry<String, List<String>> entry : customizationsMap.entrySet()) {
						OrderDto.CustomizationItem customizationItemDto = new OrderDto.CustomizationItem();

						customizationItemDto.setName(entry.getKey());

						customizationItemDto.setItems(entry.getValue());

						customizationsList.add(customizationItemDto);
					}

					orderedItem.setCustomizations(customizationsList);
				} else {
					orderedItem.setCustomizations(Collections.emptyList());
				}

				orderedItems.add(orderedItem);
			}

			orderDto.setOrderedItems(orderedItems);

			orders.add(orderDto);
		}

		orders.sort((o1, o2) -> (int) (o1.getOrderId() - o2.getOrderId()));

		return orders;
	}

	public List<CartItem> getOrderedItems(Long orderId) {
		List<CartItem> cartItems = manageOrderRepository.getOrderCartItems(orderId);

		return cartItems;
	}

	public void updateOrderStatus(Long orderId, String status, String deliveryTime) throws TFException {

		Order order = manageOrderRepository.updateOrderStatus(orderId, status);
		Customer customer = manageOrderRepository.getOrderCustomer(orderId);

		Long foodStallId = order.getFoodStallId();

		FoodStall stall = foodStallService.getFoodStallById(foodStallId);

		CustomerNotification notification = new CustomerNotification();

		notification.setFoodStallId(foodStallId);
		notification.setStallName(stall.getFoodStallName());
		notification.setNotificationStatus("ACTIVE");
		notification.setNotificationType("ORDER");
		notification.setNotificationObjectId(orderId);

		notification.setCustomerPhoneNumber(customer.getPhoneNumber());

		if (status.equalsIgnoreCase("READY")) {
			notification
					.setMessage("Your order " + order.getOrderId() + " from " + stall.getFoodStallName() + " is ready");
			notificationService.addCustomerNotification(notification);

			notificationClientService.sendMessageToCustomer(notification, customer.getPhoneNumber());
		} else if (status.equalsIgnoreCase("START_PREPARING")) {
			if (!StringUtils.hasText(deliveryTime)) {
				deliveryTime = "10";
			}
			String message = String.format("Your order %s is accepted by %s. Tentative time :%s mins",
					order.getOrderId(), stall.getFoodStallName(), deliveryTime);
			notification.setMessage(message);
			notificationService.addCustomerNotification(notification);

			notificationClientService.sendMessageToCustomer(notification, customer.getPhoneNumber());

			order.setDeliveryTime(deliveryTime);

			manageOrderRepository.updateOrder(order);
		} else if (status.equalsIgnoreCase("DELIVERED")) {
			notification
					.setMessage("Your order " + order.getOrderId() + " is delivered by " + stall.getFoodStallName());
			notificationService.addCustomerNotification(notification);

			notificationClientService.sendMessageToCustomer(notification, customer.getPhoneNumber());
		} else if (status.equalsIgnoreCase("CANCELLED")) {
			notification.setMessage("Your order " + order.getOrderId() + " is cancelled by " + stall.getFoodStallName()
					+ ". Please visit stall counter to collect the payment.");
			notificationService.addCustomerNotification(notification);

			notificationClientService.sendMessageToCustomer(notification, customer.getPhoneNumber());
		}

	}

	public List<MessageNotification> getNotifications(Long foodStallId) {
		List<MessageNotification> notofications = new ArrayList<MessageNotification>();

		return notofications;
	}

	public List<OrderFeedbackDto> getOrderFeedbacks(Long foodstallId) {
		List<OrderFeedbackDto> feedbackResponseList = new ArrayList<OrderFeedbackDto>();

		List<OrderFeedback> feedbackList = manageOrderRepository.getFeedbacks(foodstallId);
		for (OrderFeedback feedback : feedbackList) {

			OrderFeedbackDto orderFeedbackResponseDto = new OrderFeedbackDto();
			orderFeedbackResponseDto.setPhoneNumber(feedback.getCustomerPhoneNumber());
			orderFeedbackResponseDto.setOrderId(feedback.getOrderId());

			Order order = manageOrderRepository.getOrder(feedback.getOrderId());
			Customer customer = manageOrderRepository.getOrderCustomer(order.getOrderId());

			orderFeedbackResponseDto.setOrderDate(order.getOrderedTime());
			orderFeedbackResponseDto.setCustomerName(customer.getFullName());
			orderFeedbackResponseDto.setEmail(customer.getEmail());

			List<CartItem> orderedItems = manageOrderRepository.getOrderCartItems(feedback.getOrderId());

			List<String> items = orderedItems.stream().map(item -> item.getItemName()).collect(Collectors.toList());
			orderFeedbackResponseDto.setItems(items);

			int totalRating = 0;

			for (int rating : feedback.getRatings().values()) {
				totalRating += rating;
			}

			orderFeedbackResponseDto.setReview(feedback.getReview());
			orderFeedbackResponseDto.setRatingVal(totalRating / feedback.getRatings().size());

			feedbackResponseList.add(orderFeedbackResponseDto);
		}

		return feedbackResponseList;
	}

	public List<OrderFeedbackDto> getOrderComplaints(Long foodstallId) {
		List<OrderFeedbackDto> feedbackResponseList = new ArrayList<OrderFeedbackDto>();

		List<CustomerComplaints> complaints = manageOrderRepository.getCustomerComplaints(foodstallId);
		for (CustomerComplaints complaint : complaints) {

			Order order = manageOrderRepository.getOrder(complaint.getOrderId());

			Customer customer = manageOrderRepository.getOrderCustomer(order.getOrderId());

			OrderFeedbackDto orderFeedbackResponseDto = new OrderFeedbackDto();
			orderFeedbackResponseDto.setPhoneNumber(complaint.getCustomerPhoneNumber());
			orderFeedbackResponseDto.setOrderId(complaint.getOrderId());
			orderFeedbackResponseDto.setCustomerName(customer.getFullName());

			orderFeedbackResponseDto.setOrderDate(order.getOrderedTime());

			orderFeedbackResponseDto.setReview(complaint.getReview());

			feedbackResponseList.add(orderFeedbackResponseDto);
		}

		return feedbackResponseList;
	}

	public String updatepaytmPaymentdetails(String date) throws Exception {
	    String baseUrl = "https://securegw.paytm.in/merchant-settlement-service/settlement/list";
	    String checksumKey = "0AOmW1nGtH9MSvgA";
	    int pageNum = 1;
	    String responseData = "";
	    int pageSize=500;

	    while (true) {
	        TreeMap<String, String> paytmParams = new TreeMap<String, String>();
	        paytmParams.put("MID", "ykrjMy07609170067260");
	        paytmParams.put("utrProcessedStartTime", date);
	        paytmParams.put("pageNum", String.valueOf(pageNum));
	        paytmParams.put("pageSize", String.valueOf(pageSize));

	        String checksum = PaytmChecksum.generateSignature(paytmParams, checksumKey);
	        paytmParams.put("checksumHash", checksum);

	        JSONObject obj = new JSONObject(paytmParams);
	        String post_data = obj.toString();

	        try {
	            URL url = new URL(baseUrl);

	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("POST");
	            connection.setRequestProperty("Content-Type", "application/json");
	            connection.setDoOutput(true);

	            DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
	            requestWriter.writeBytes(post_data);
	            requestWriter.close();

	            InputStream is = connection.getInputStream();
	            BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));

	            if ((responseData = responseReader.readLine()) != null) {
	                ObjectMapper objectMapper = new ObjectMapper();
	                PaytmTransactionsResponse apiResponse = objectMapper.readValue(responseData, PaytmTransactionsResponse.class);

	                SettlementListResponse settlementListResponse = apiResponse.getSettlementListResponse();
	                List<SettlementTransaction> settlementTransactionList = settlementListResponse.getSettlementTransactionList();
	                System.out.println("Total Size: " + settlementTransactionList.size());

	                if (settlementTransactionList.isEmpty()) {
	                    // No more records, exit the loop
	                    break;
	                }

	                for (SettlementTransaction transaction : settlementTransactionList) {
	                	String oriderId = transaction.getORDERID();
						String orderGrossAmount = transaction.getTXNAMOUNT();
						String taxes = transaction.getGST();
						String orderTotalAmount = transaction.getSETTLEDAMOUNT();
						String paymentMode = transaction.getPAYMENTMODE();
						String transactionID = transaction.getTXNID();
						String commission = transaction.getCOMMISSION();
						String settlementAmount = transaction.getSETTLEDAMOUNT();
						Order order = new Order();
						long orderID = Long.parseLong(oriderId);
						System.out.println(orderID);
						order = getOrder(orderID);

						if (order != null) {
							System.out.println(order);
							TreeMap<String, String> transactionParameters = new TreeMap<>();
							transactionParameters.put("PaymentMode", paymentMode);
							transactionParameters.put("Commission", transaction.getCOMMISSION());
							transactionParameters.put("SettlementAmount", settlementAmount);
							transactionParameters.put("gst", taxes);
							System.out.println(order.getPayTmTransactionParameters());
//							order.getPayTmTransactionParameters().forEach((k, v) -> System.out.println(k + v));
							if(order.getPayTmTransactionParameters() != null) {
								order.getPayTmTransactionParameters().forEach((k, v) -> transactionParameters.put(k, v));
							}
							

							order.setPayTmTransactionParameters(transactionParameters);

							manageOrderRepository.updateOrder(order);

//							System.out.println(oriderId + "\t" + orderGrossAmount + "\t" + taxes + "\t"
//									+ orderTotalAmount + "\t" + paymentMode + "\t" + transactionID + "\t"
//									+ commission + "\t" + settlementAmount);
						}
	                }
	            }
	            responseReader.close();
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }

	        pageNum++; // Increment pageNum for the next page
	    }

	    return responseData;
	}


	 public static PaytmTransactionResponse transactionStatus(Long orderId ) {
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
	            		
	            	 System.out.println(responseData);
	            	PaytmTransactionResponse paytmResPonse = object.readValue(responseData, PaytmTransactionResponse.class);
	            	
	            	
	            System.out.println(paytmResPonse);
	                return paytmResPonse;
	            }
	            responseReader.close();
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }

	        // Return null if there was an error.
	        return null;
	    }
	public Order getOrder(Long orderId) {
		return manageOrderRepository.getOrder(orderId);
	}
}
