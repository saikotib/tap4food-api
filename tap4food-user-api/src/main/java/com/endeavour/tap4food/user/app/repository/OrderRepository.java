package com.endeavour.tap4food.user.app.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.order.CartItem;
import com.endeavour.tap4food.app.model.order.CartItemCustomization;
import com.endeavour.tap4food.app.model.order.Customer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.service.CommonSequenceService;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class OrderRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private CommonSequenceService commonSequenceService;
	
	public Order placeOrder(Order order) {
		mongoTemplate.save(order);
		
		log.info("Order is placed. Data : {}", order);
		return order;
	}
	
	public Customer saveCustomer(Customer customer) {
		mongoTemplate.save(customer);
		
		log.info("Customer Data is saved. {} ", customer);
		return customer;
	}
	
	public void saveCartItem(CartItem cartItem) {

		mongoTemplate.save(cartItem);
		
	}
	
	public void saveCartItemCustomizations(CartItemCustomization cartItemCustomization) {

		mongoTemplate.save(cartItemCustomization);
	}
	
	public void saveOrderedCustomer(Customer customer) {

		mongoTemplate.save(customer);
	}
	
	public Long getNewOrderId() {

		Long orderId = commonSequenceService
				.getNextSequence(MongoCollectionConstant.COLLECTION_ORDER_SEQ);

		return orderId;
	}
	
	public Long getNewOrderItemSeq() {

		Long orderItemSeq = commonSequenceService
				.getNextSequence(MongoCollectionConstant.COLLECTION_ORDER_ITEM_SEQ);

		return orderItemSeq;
	}
	
	public List<Customer> getCustomerOrders(String phoneNumber){
		Query query = new Query(Criteria.where("phoneNumber").is(phoneNumber));
		
		List<Customer> customerOrders = mongoTemplate.find(query, Customer.class);
		
		return customerOrders;
	}
	
	public List<Order> getCustomerOrders(List<Long> orderIdList){
		
		Query query = new Query(Criteria.where("orderId").in(orderIdList));
		
		List<Order> orders = mongoTemplate.find(query, Order.class);
		
		return orders;
	}
}
