package com.endeavour.tap4food.user.app.repository;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.offer.FoodItemsList;
import com.endeavour.tap4food.app.model.offer.Offer;

@Repository
public class CartRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public Offer getFoodItemAssociatedOffer(Long foodItemId) {
		
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		List<FoodItemsList> offerFoodItems = mongoTemplate.find(query, FoodItemsList.class);
		
		if(ObjectUtils.isEmpty(offerFoodItems)) {
			return null;
		}else {
			Long offerId = offerFoodItems.get(0).getOfferId();
			
			Offer offer = this.getOffer(offerId);
			return offer;
		}
	}
	
	public Offer getOffer(Long offerId) {
		
		Query query = new Query(Criteria.where("offerId").is(offerId));
		Offer offer = mongoTemplate.findOne(query, Offer.class);
		
		return offer;
	}
	
}
