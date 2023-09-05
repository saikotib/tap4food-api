package com.endeavour.tap4food.merchant.app.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.model.fooditem.PreProcessedFoodItems;

import lombok.extern.slf4j.Slf4j;

@Repository
public class PreProcessorRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public PreProcessedFoodItems getPreProcessedItems(Long foodStallId) {
		Query query = new Query(Criteria.where("foodStallId").is(foodStallId));
		
		return mongoTemplate.findOne(query, PreProcessedFoodItems.class);
	}
	
	public void saveData(PreProcessedFoodItems data) {
		
		mongoTemplate.save(data);
	}
}
