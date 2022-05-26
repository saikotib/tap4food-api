package com.endeavour.tap4food.user.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.CustomerComplaints;
import com.endeavour.tap4food.app.model.OrderFeedback;
import com.endeavour.tap4food.user.app.repository.FeedbackRepository;

@Service
public class FeedbackService {

	@Autowired
	private FeedbackRepository feedbackRepository;
	
	public OrderFeedback createReview(OrderFeedback feedback) {
		
		feedbackRepository.createReview(feedback);	
		
		Map<Long, Integer> ratings = feedback.getRatings();
		
		for(Map.Entry<Long, Integer> entry : ratings.entrySet()) {
			Long itemId = entry.getKey();
			Integer ratingVal = entry.getValue();
			
			try {
				feedbackRepository.updateFoodItemRating(itemId, ratingVal);
			} catch (TFException e) {
				e.printStackTrace();
			}
		}
		
		return feedback;
	}
	
	public CustomerComplaints createComplaint(CustomerComplaints complaint) {
		
		feedbackRepository.createComplaint(complaint);	
				
		return complaint;
	}
	
	public void uploadFiles(String reviewId, List<MultipartFile> files) {
		
		for(MultipartFile file : files) {
			String fileId = feedbackRepository.uploadFile(file);
			
			System.out.println(fileId);
		}
		
	}
	
	
}
