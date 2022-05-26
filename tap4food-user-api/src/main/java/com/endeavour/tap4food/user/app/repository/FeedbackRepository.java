package com.endeavour.tap4food.user.app.repository;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.CustomerComplaints;
import com.endeavour.tap4food.app.model.OrderFeedback;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Repository
public class FeedbackRepository {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private GridFsTemplate gridFsTemplate;
	
	public OrderFeedback createReview(OrderFeedback feedback) {
		System.out.println(feedback);
		mongoTemplate.save(feedback);		
		return feedback;
	}
	
	public CustomerComplaints createComplaint(CustomerComplaints complaint) {
		System.out.println(complaint);
		mongoTemplate.save(complaint);		
		return complaint;
	}
	
	public OrderFeedback getReview(String id) {
		
		Query query = new Query(Criteria.where("_id").is(id));
		
		OrderFeedback feedback = mongoTemplate.findOne(query, OrderFeedback.class);
		
		return feedback;
	}
	
	public String uploadFile(MultipartFile file) {
		
		DBObject metadata = new BasicDBObject();
        metadata.put("fileSize", file.getSize());
        
        Object fileID = null;
		try {
			fileID = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metadata);
		} catch (IOException e) {
			e.printStackTrace();
		}

        return fileID.toString();
	}
	
	public FoodItem getFoodItem(Long foodItemId) throws TFException {
		
		Query query = new Query(Criteria.where("foodItemId").is(foodItemId));
		
		FoodItem foodItem = mongoTemplate.findOne(query, FoodItem.class);
		
		return foodItem;
	}
	
	public void updateFoodItem(FoodItem foodItem) throws TFException {
		
		mongoTemplate.save(foodItem);
		
	}
	
	public void updateFoodItemRating(Long itemId, int newRatingVal) throws TFException {
		
		FoodItem item = this.getFoodItem(itemId);
		
		Double ratingVal = item.getRating();
		Long totalReviews = item.getTotalReviews() == 1 ? 0 : item.getTotalReviews();
		
		if(ratingVal == null) {
			ratingVal = Double.valueOf(0);
		}
		
		Double distributedRatingVal = ratingVal * totalReviews;
		
		Double accumulatedRating = (distributedRatingVal + newRatingVal) / (totalReviews + 1);
		
		item.setRating(accumulatedRating);
		item.setTotalReviews(totalReviews + 1);
		
		this.updateFoodItem(item);
	}
}
