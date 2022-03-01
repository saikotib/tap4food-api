package com.endeavour.tap4food.merchant.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.notifications.CustomerNotification;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.model.order.CartItem;
import com.endeavour.tap4food.app.model.order.CartItemCustomization;
import com.endeavour.tap4food.app.model.order.Customer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.model.order.OrderedOfferItems;
import com.endeavour.tap4food.app.response.dto.OrderDto;
import com.endeavour.tap4food.app.service.NotificationService;
import com.endeavour.tap4food.merchant.app.repository.ManageOrderRepository;

@Service
public class ManageOrderService {

	@Autowired
	private ManageOrderRepository manageOrderRepository;
	
	@Autowired
	private FoodStallService foodStallService;
	
	@Autowired
	private NotificationService notificationService;
	
	public Map<String, List<OrderDto>> getOrders(Long foodStallId) {
		
		Map<String, List<OrderDto>> ordersMap = new HashMap<String, List<OrderDto>>();
		
		List<OrderDto> orders = new ArrayList<OrderDto>();
		
		List<Order> allOrders = manageOrderRepository.getOrders(foodStallId);
		
		for(Order order : allOrders) {
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
			
			FoodStall foodStall = foodStallService.getFoodStallById(foodStallId);
			
			orderDto.setFoodStallName(foodStall.getFoodStallName());
			
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
			
			List<CartItem> cartItems = this.getOrderedItems(order.getOrderId());
			
			List<OrderDto.OrderedItem> orderedItems = new ArrayList<OrderDto.OrderedItem>();
			
			for(CartItem item : cartItems) {
				
				System.out.println("Cart Item : " + item);
				
				OrderDto.OrderedItem orderedItem = new OrderDto.OrderedItem();
				
				orderedItem.setItemId(item.getFoodItemId());
				orderedItem.setItemName(item.getItemName());
				orderedItem.setPrice(item.getFinalPrice());
				orderedItem.setQuantity(item.getQuantity());
				orderedItem.setCustomizationFlag(item.isCustomizationFlag());
				
				boolean isOffer = item.isOffer();
				
				if(isOffer) {
					List<OrderedOfferItems> orderedOfferItems = manageOrderRepository.getOrderOfferItems(item.getCartItemId());
					
					Map<String, List<String>> itemsMap = new HashMap<String, List<String>>();
					
					List<OrderDto.CustomizationItem> customizationsList = new ArrayList<OrderDto.CustomizationItem>();
					
					for(OrderedOfferItems orderedOfferItem : orderedOfferItems) {
						if(!itemsMap.containsKey(orderedOfferItem.getFoodItemName())) {
							itemsMap.put(orderedOfferItem.getFoodItemName(), new ArrayList<String>());
						}
						
						itemsMap.get(orderedOfferItem.getFoodItemName()).add(orderedOfferItem.getCustomizationItem());
					}
					
					for(Map.Entry<String, List<String>> entry : itemsMap.entrySet()) {
						OrderDto.CustomizationItem customizationItemDto = new OrderDto.CustomizationItem();
						
						customizationItemDto.setName(entry.getKey());
						
						customizationItemDto.setItems(entry.getValue());
						
						customizationsList.add(customizationItemDto);
					}
					
					orderedItem.setCustomizations(customizationsList);
				}else {
					if(item.isCustomizationFlag()) {
						List<OrderDto.CustomizationItem> customizationsList = new ArrayList<OrderDto.CustomizationItem>();
						List<CartItemCustomization> customizations = manageOrderRepository.getOrderItemCustomizations(item.getCartItemId());
						
						Map<String, List<String>> customizationsMap = new HashMap<String, List<String>>();
						
						for(CartItemCustomization customization : customizations) {
							if(!customizationsMap.containsKey(customization.getCustomizationName())) {
								customizationsMap.put(customization.getCustomizationName(), new ArrayList<String>());
							}
							
							System.out.println("Cart Item customization: " + customization);
							
							String items = customization.getCustomizationItem();
							
							List<String> itemsList = new ArrayList<String>();
							
							System.out.println("Items : " + items);
							
							if(Objects.isNull(items)) {
								continue;
							}
							
							if(items.indexOf("###") > -1) {
								String itemTokens[] = items.split("###");
								
								for(String token : itemTokens) {
									itemsList.add(token);
								}
							}else {
								itemsList.add(items);
							}
							
							customizationsMap.get(customization.getCustomizationName()).addAll(itemsList);
						}
						
						for(Map.Entry<String, List<String>> entry : customizationsMap.entrySet()) {
							OrderDto.CustomizationItem customizationItemDto = new OrderDto.CustomizationItem();
							
							customizationItemDto.setName(entry.getKey());
							
							customizationItemDto.setItems(entry.getValue());
							
							customizationsList.add(customizationItemDto);
						}
						
						orderedItem.setCustomizations(customizationsList);
					}else {
						orderedItem.setCustomizations(Collections.emptyList());
					}
				}
				
				
				
				orderedItems.add(orderedItem);
			}
			
			orderDto.setOrderedItems(orderedItems);
			
			if(!ordersMap.containsKey(orderDto.getStatus())){
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
		
		for(Order order : allOrders) {
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
			
			FoodStall foodStall = foodStallService.getFoodStallById(foodStallId);
			
			orderDto.setFoodStallName(foodStall.getFoodStallName());
			
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
			
			List<CartItem> cartItems = this.getOrderedItems(order.getOrderId());
			
			List<OrderDto.OrderedItem> orderedItems = new ArrayList<OrderDto.OrderedItem>();
			
			for(CartItem item : cartItems) {
				
				System.out.println("Cart Item : " + item);
				
				OrderDto.OrderedItem orderedItem = new OrderDto.OrderedItem();
				
				orderedItem.setItemId(item.getFoodItemId());
				orderedItem.setItemName(item.getItemName());
				orderedItem.setPrice(item.getFinalPrice());
				orderedItem.setQuantity(item.getQuantity());
				orderedItem.setCustomizationFlag(item.isCustomizationFlag());
				
				if(item.isCustomizationFlag()) {
					List<OrderDto.CustomizationItem> customizationsList = new ArrayList<OrderDto.CustomizationItem>();
					List<CartItemCustomization> customizations = manageOrderRepository.getOrderItemCustomizations(item.getCartItemId());
					
					Map<String, List<String>> customizationsMap = new HashMap<String, List<String>>();
					
					for(CartItemCustomization customization : customizations) {
						if(!customizationsMap.containsKey(customization.getCustomizationName())) {
							customizationsMap.put(customization.getCustomizationName(), new ArrayList<String>());
						}
						
						System.out.println("Cart Item customization: " + customization);
						
						String items = customization.getCustomizationItem();
						
						List<String> itemsList = new ArrayList<String>();
						
						System.out.println("Items : " + items);
						
						if(Objects.isNull(items)) {
							continue;
						}
						
						if(items.indexOf("###") > -1) {
							String itemTokens[] = items.split("###");
							
							for(String token : itemTokens) {
								itemsList.add(token);
							}
						}else {
							itemsList.add(items);
						}
						
						customizationsMap.get(customization.getCustomizationName()).addAll(itemsList);
					}
					
					for(Map.Entry<String, List<String>> entry : customizationsMap.entrySet()) {
						OrderDto.CustomizationItem customizationItemDto = new OrderDto.CustomizationItem();
						
						customizationItemDto.setName(entry.getKey());
						
						customizationItemDto.setItems(entry.getValue());
						
						customizationsList.add(customizationItemDto);
					}
					
					orderedItem.setCustomizations(customizationsList);
				}else {
					orderedItem.setCustomizations(Collections.emptyList());
				}
				
				orderedItems.add(orderedItem);
			}
			
			orderDto.setOrderedItems(orderedItems);
			
			orders.add(orderDto);
		}
	
		return orders;
	}
	
	public List<CartItem> getOrderedItems(Long orderId){
		List<CartItem> cartItems = manageOrderRepository.getOrderCartItems(orderId);
		
		return cartItems;
	}
	
	public void updateOrderStatus(Long orderId, String status) throws TFException {
		
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
		
		if(status.equalsIgnoreCase("READY")) {
			notification.setMessage("Your order " + order.getOrderId() + " from " + stall.getFoodStallName() + " is ready");
		}else if(status.equalsIgnoreCase("START_PREPARING")) {
			notification.setMessage("Your order " + order.getOrderId() + " is accepted by " + stall.getFoodStallName());
		}else if(status.equalsIgnoreCase("DELIVERED")) {
			notification.setMessage("Your order " + order.getOrderId() + " is delovered by " + stall.getFoodStallName());
		}else if(status.equalsIgnoreCase("CANCELLED")) {
			notification.setMessage("Your order " + order.getOrderId() + " is cancelled by " + stall.getFoodStallName());
		}
		
		notification.setCustomerPhoneNumber(customer.getPhoneNumber());
		
		notificationService.addCustomerNotification(notification);
				
	}
	
	public List<MessageNotification> getNotifications(Long foodStallId){
		List<MessageNotification> notofications = new ArrayList<MessageNotification>();
		
		return notofications;
	}
}
