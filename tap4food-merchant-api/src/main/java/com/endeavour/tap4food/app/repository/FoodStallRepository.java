package com.endeavour.tap4food.app.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.collection.constants.FoodStallCollectionConstants;
import com.endeavour.tap4food.app.service.FoodStalNextSequenceService;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

@Repository
public class FoodStallRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private FoodStalNextSequenceService foodStalNextSequenceService;
	
	@Autowired
	private MerchantRepository merchantRepository;
	
	public boolean isFoodStallFound(Long foodStallId) {
		boolean merchantExists = false;
		
		Query query = new Query(Criteria.where(FoodStallCollectionConstants.FOOD_STALL_NUMBER).is(foodStallId));
		
		merchantExists = mongoTemplate.exists(query, Merchant.class);
		
		return merchantExists;
	}
	
	public FoodStall createNewFoodStall(Long merchantId, FoodStall foodStall) throws TFException {

		Optional<Merchant> merchantData = merchantRepository.findMerchantByUniqueId(merchantId);

		if(!merchantData.isPresent()) {
			throw new TFException("Merchant not found");
		}
		
		foodStall.setMerchantUniqueNumber(merchantId);
		foodStall.setFoodStallId(getIdForNewFoodStall());
		
		mongoTemplate.save(foodStall);
		
		Merchant merchant = merchantData.get();
		
		List<FoodStall> foodStalls = merchant.getFoodStalls();

		if(Objects.isNull(foodStalls)) {
			foodStalls = new ArrayList<FoodStall>();
		}

		foodStalls.add(foodStall);
		
		merchant.setFoodStalls(foodStalls);
		
		mongoTemplate.save(merchant);
		
		return foodStall;
	}
	
	private Long getIdForNewFoodStall() {
		
		Long foodStallID = foodStalNextSequenceService.getNextSequence(MongoCollectionConstant.COLLECTION_FOODSTALL_SEQ);
		
		return foodStallID;
	}
}
