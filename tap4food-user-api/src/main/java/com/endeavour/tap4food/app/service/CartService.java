package com.endeavour.tap4food.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.offer.Offer;
import com.endeavour.tap4food.app.repository.CartRepository;

@Service
public class CartService {

	@Autowired
	private CartRepository cartRepository;

	public Offer getFoodItemAssociatedOffer(Long foodItemId) {
		Offer offer = cartRepository.getFoodItemAssociatedOffer(foodItemId);

		return offer;
	}
}
