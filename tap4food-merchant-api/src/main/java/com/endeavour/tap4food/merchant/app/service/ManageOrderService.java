package com.endeavour.tap4food.merchant.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.model.order.CartItem;
import com.endeavour.tap4food.app.model.order.CartItemCustomization;
import com.endeavour.tap4food.app.model.order.Customer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.response.dto.OrderDto;
import com.endeavour.tap4food.merchant.app.repository.ManageOrderRepository;

@Service
public class ManageOrderService {

	@Autowired
	private ManageOrderRepository manageOrderRepository;
	
	@Autowired
	private FoodStallService foodStallService;
	
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
						
						String items = customization.getCustomizationItem();
						
						List<String> itemsList = new ArrayList<String>();
						
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
			
			if(!ordersMap.containsKey(orderDto.getStatus())){
				ordersMap.put(orderDto.getStatus(), new ArrayList<OrderDto>());
			}
			
			ordersMap.get(orderDto.getStatus()).add(orderDto);
			
			orders.add(orderDto);
		}
		
		List<MessageNotification> notifications = this.getNotifications(foodStallId);
		
		for(MessageNotification notification : notifications) {
			manageOrderRepository.inactivateNotification(foodStallId, notification.getOrderId());
		}
		
		return ordersMap;
	}
	
	public List<CartItem> getOrderedItems(Long orderId){
		List<CartItem> cartItems = manageOrderRepository.getOrderCartItems(orderId);
		
		return cartItems;
	}
	
	public void updateOrderStatus(Long orderId, String status) throws TFException {
		
		manageOrderRepository.updateOrderStatus(orderId, status);
		
	}
	
	public List<MessageNotification> getNotifications(Long foodStallId){
		List<MessageNotification> notofications = new ArrayList<MessageNotification>();
		
		return notofications;
	}
}
