package com.endeavour.tap4food.user.app.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.order.CartItem;
import com.endeavour.tap4food.app.model.order.CartItemCustomization;
import com.endeavour.tap4food.app.model.order.Customer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.request.dto.PlaceOrderRequest;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.user.app.repository.OrderRepository;

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
					custumization.setCustomizationItem(selectedItemCustomization.getCustomizationItem());
					custumization.setCustomizationName(selectedItemCustomization.getCustomizationName());
					custumization.setPrice(selectedItemCustomization.getPrice());
					
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
		
		return order;
	}
}
