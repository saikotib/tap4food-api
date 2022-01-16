package com.endeavour.tap4food.user.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.notifications.MessageNotification;
import com.endeavour.tap4food.app.model.order.CartItem;
import com.endeavour.tap4food.app.model.order.CartItemCustomization;
import com.endeavour.tap4food.app.model.order.Customer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.request.dto.PlaceOrderRequest;
import com.endeavour.tap4food.app.response.dto.OrderDto;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.user.app.repository.OrderRepository;
import com.endeavour.tap4food.user.app.response.dto.OrderResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {
	
	@Autowired
	private OrderRepository orderRepository;

	public Order placeOrder(PlaceOrderRequest orderRequest) {
		
		log.info("Input order request : {}", orderRequest);
		
		Order order = new Order();
		
		Long newOrderSeq = orderRepository.getNewOrderId();
		
		order.setId(newOrderSeq);
		order.setOrderId(newOrderSeq + 100000);
		order.setGrandTotal(orderRequest.getGrandTotal());
		order.setOrderedTime(DateUtil.getPresentDateAndTime());
		order.setSelfPickup(orderRequest.isSelfPickup());
		order.setFoodStallId(orderRequest.getFoodStallId());
		
		if(orderRequest.isTheatre()) {
			order.setTheatre(true);
			
			if(!orderRequest.isSelfPickup())
				order.setScreenNumber(orderRequest.getScreenNumber());
		}
		if(!orderRequest.isSelfPickup())
			order.setSeatNumber(orderRequest.getSeatNumber());
		
		order.setStatus("NEW");
		order.setSubTotalAmount(orderRequest.getSubTotalAmount());
		order.setTaxAmount(orderRequest.getTaxAmount());
		
		List<PlaceOrderRequest.SelectedCartItem> selectedCartItems = orderRequest.getCartItems();
		int totalItems = 0;
		for(PlaceOrderRequest.SelectedCartItem cartItem : selectedCartItems) {
			totalItems += cartItem.getQuantity();
		}
		
		order.setTotalItems(totalItems);
		
		order = orderRepository.placeOrder(order);
		log.info("Order Is placed. {}", order);
		
		for(PlaceOrderRequest.SelectedCartItem selectedCartItem : selectedCartItems) {
			
			Long newOrderItemSeq = orderRepository.getNewOrderItemSeq();
			
			CartItem cartItem = new CartItem();
			cartItem.setCartItemId(newOrderItemSeq);
			cartItem.setFoodItemId(selectedCartItem.getFoodItemId());
			cartItem.setCustomizationFlag(selectedCartItem.isCustomizationFlag());
			cartItem.setItemName(selectedCartItem.getItemName());
			cartItem.setOrderId(order.getOrderId());
			cartItem.setPizza(selectedCartItem.isPizza());
			cartItem.setQuantity(selectedCartItem.getQuantity());
			cartItem.setFinalPrice(selectedCartItem.getFinalPrice());
			
			orderRepository.saveCartItem(cartItem);
			
			if(selectedCartItem.isCustomizationFlag()) {
				List<PlaceOrderRequest.CartItemCustomization> customizations = selectedCartItem.getCustomizations();
				
				for(PlaceOrderRequest.CartItemCustomization selectedItemCustomization : customizations) {
					CartItemCustomization custumization = new CartItemCustomization();
					custumization.setCartItemId(newOrderItemSeq);
					custumization.setCustomizationItem(selectedItemCustomization.getItem());
					custumization.setCustomizationName(selectedItemCustomization.getKey());
					
					orderRepository.saveCartItemCustomizations(custumization);
				}
			}
		}
		
		Customer customer = new Customer();
		customer.setEmail(orderRequest.getCustomer().getEmail());
		customer.setFullName(orderRequest.getCustomer().getFullName());
		customer.setOrderId(order.getOrderId());
		customer.setPhoneNumber(orderRequest.getCustomer().getPhoneNumber());
		customer.setId(newOrderSeq);
		
		orderRepository.saveCustomer(customer);
		
		this.addToNotifications(orderRequest.getCustomer().getPhoneNumber(), order.getFoodStallId(), "New order is placed", order.getOrderId(), "NEW");
		
		return order;
	}
	
	private void addToNotifications(String phoneNumber, Long foodStallId, String message, Long orderId, String orderStatus) {
		MessageNotification notification = new MessageNotification();
		
		notification.setCustomerPhoneNumber(phoneNumber);
		notification.setFoodStallId(foodStallId);
		notification.setMessage(message);
		notification.setNotificationStatus("ACTIVE");
		notification.setOrderId(orderId);
		notification.setOrderStatus(orderStatus);
	}
	
	public List<OrderDto> getOrders(String phoneNumber, Long fsId){
		
		List<OrderDto> orders = new ArrayList<OrderDto>();
		
		List<Customer> customers = orderRepository.getCustomerOrders(phoneNumber);
		
		Map<Long, Customer> customerMap = new HashMap<Long, Customer>();
		
		List<Long> orderIdList = new ArrayList<Long>();
		
		for(Customer customer : customers) {
			orderIdList.add(customer.getOrderId());
			customerMap.put(customer.getOrderId(), customer);
		}
		
		List<Order> existingOrders = orderRepository.getCustomerOrders(orderIdList);
		
		for(Order order : existingOrders) {
			
			if(!order.getFoodStallId().equals(fsId)) {
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
			
			FoodStall foodStall = orderRepository.getFoodStall(fsId);
			
			orderDto.setFoodStallName(foodStall.getFoodStallName());
			
			Customer customer = customerMap.get(order.getOrderId());
			
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
					List<CartItemCustomization> customizations = orderRepository.getOrderItemCustomizations(item.getCartItemId());
					
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
		List<CartItem> cartItems = orderRepository.getOrderCartItems(orderId);
		
		return cartItems;
	}
	
}
