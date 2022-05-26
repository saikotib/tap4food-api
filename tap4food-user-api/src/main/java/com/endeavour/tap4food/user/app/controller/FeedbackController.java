package com.endeavour.tap4food.user.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.model.CustomerComplaints;
import com.endeavour.tap4food.app.model.OrderFeedback;
import com.endeavour.tap4food.user.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.user.app.service.FeedbackService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/customer/feedback")
@Api(tags = "FeedbackController")
public class FeedbackController {
	
	@Autowired
	private FeedbackService feedbackService;

	@RequestMapping(value = "/createReview", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> postReview(@RequestBody OrderFeedback feedback){
		
		feedback = feedbackService.createReview(feedback);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.data(feedback)
				.status("success")
				.build();
		
		return ResponseEntity.ok().body(responseHolder);
	}
	
	@RequestMapping(value = "/{reviewId}/saveReviewFiles", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveReviewFiles(@PathVariable("reviewId") String reviewId, 
			@RequestParam("files") List<MultipartFile> files){
		
		feedbackService.uploadFiles(reviewId, files);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.data("uploaded..")
				.status("success")
				.build();
		
		return ResponseEntity.ok().body(responseHolder);
	}
	
	@RequestMapping(value = "/createComplaint", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createComplaint(@RequestBody CustomerComplaints complaint){
		
		complaint = feedbackService.createComplaint(complaint);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.data(complaint)
				.status("success")
				.build();
		
		return ResponseEntity.ok().body(responseHolder);
	}
}
