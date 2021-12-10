package com.endeavour.tap4food.merchant.app.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.offer.FoodItemsList;
import com.endeavour.tap4food.app.model.offer.Offer;
import com.endeavour.tap4food.app.model.offer.SuggestionItem;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class OfferRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public Offer createOffer(Offer offer) {
		
		mongoTemplate.save(offer);
		
		log.info("Offer is created succesfully.");
		return offer;
	}
	
	public void updateOfferImage(String offerImage, Long offerId) {
		
		Query query = new Query(Criteria.where("offerId").is(offerId));
		
		Offer offer = mongoTemplate.findOne(query, Offer.class);
		
		offer.setOfferImage(offerImage);
		
		mongoTemplate.save(offer);
		log.info("Offer pic is uploaded : " + offerImage);
	}
	
	public List<Offer> getOffers(Long fsId){
		
		Query query = new Query(Criteria.where("fsId").is(fsId));
		
		List<Offer> offers = mongoTemplate.find(query, Offer.class);
	
		return offers;
	}
	
	public Offer getOffer(Long fsId, Long offerId){
		
		Query query = new Query(Criteria.where("fsId").is(fsId).andOperator(Criteria.where("offerId").is(offerId)));
		
		Offer offer = mongoTemplate.findOne(query, Offer.class);
	
		return offer;
	}
	
	public List<FoodItemsList> getOfferFoodItemLists(Long offerId){
		
		Query query = new Query(Criteria.where("offerId").is(offerId));
		
		List<FoodItemsList> offerItemsLists = mongoTemplate.find(query, FoodItemsList.class);
	
		return offerItemsLists;
	}
	
	public void createOfferFoodItemsList(FoodItemsList foodItemsList) {
		
		mongoTemplate.save(foodItemsList);
		
		log.info("Offer fooditems list is created succesfully.");
	}
	
	public void createOfferSuggestionFoodItem(SuggestionItem suggestItem) {
		
		mongoTemplate.save(suggestItem);
		
		log.info("Offer is created succesfully.");
	}
}
