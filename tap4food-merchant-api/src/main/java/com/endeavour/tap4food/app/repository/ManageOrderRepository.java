package com.endeavour.tap4food.app.repository;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.order.CartItem;
import com.endeavour.tap4food.app.model.order.Customer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.request.dto.PlaceOrderRequest.CartItemCustomization;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ManageOrderRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public List<Order> getOrders(String status) {
		Query query = new Query(Criteria.where("status").is(status.toUpperCase()));
		
		List<Order> orders = mongoTemplate.find(query, Order.class);
		
		return orders;
	}
	
	public List<Order> getOrders(Long foodStallId) {
		
		Query query = new Query(Criteria.where("foodStallId").is(foodStallId));
		
		List<Order> orders = mongoTemplate.find(query, Order.class);
		
		return orders;
	}
	
	public Order getOrder(Long orderId) {
		Query query = new Query(Criteria.where("orderId").is(orderId));
		
		Order order = mongoTemplate.findOne(query, Order.class);
		
		return order;
	}
	
	public Customer getOrderCustomer(Long orderId) {
		Query query = new Query(Criteria.where("orderId").is(orderId));
		
		Customer orderCustomer = mongoTemplate.findOne(query, Customer.class);
		
		return orderCustomer;
	}
	
	public List<CartItem> getOrderCartItems(Long orderId) {
		Query query = new Query(Criteria.where("orderId").is(orderId));
		
		List<CartItem> cartItems = mongoTemplate.find(query, CartItem.class);
		
		return cartItems;
	}
	
	public List<CartItemCustomization> getOrderItemCustomizations(Long cartItemId) {
		Query query = new Query(Criteria.where("cartItemId").is(cartItemId));
		
		List<CartItemCustomization> cartItemCustomizations = mongoTemplate.find(query, CartItemCustomization.class);
		
		return cartItemCustomizations;
	}
	
	public Order updateOrderStatus(Long orderId, String status) throws TFException {
		Order order = this.getOrder(orderId);
		
		if(Objects.isNull(order)) {
			throw new TFException("Invalid order Id");
		}
		
		order.setStatus(status);
		
		mongoTemplate.save(order);
		
		log.info("Order status is updated : {}", status);
		return order;
	} 
}
