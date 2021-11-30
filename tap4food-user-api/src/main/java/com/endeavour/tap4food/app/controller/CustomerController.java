package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.offer.Offer;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.payload.request.ProfileUpdateRequest;
import com.endeavour.tap4food.app.response.dto.CustomizationResponse;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.security.model.User;
import com.endeavour.tap4food.app.service.CartService;
import com.endeavour.tap4food.app.service.CustomerService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/customer")
@Api(tags = "CustomerController", description = "All user operations are available here")
public class CustomerController {
	
	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private CartService cartService;

	@RequestMapping(value = "/view-otp", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> login(@RequestParam("phone-number") String phoneNumber) {

		Otp otp = customerService.fetchOtp(phoneNumber);
		ResponseHolder response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data(otp)
					.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/verify-otp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> verifyOtp(@RequestParam("phone-number") String phoneNumber,
			@RequestParam("input-otp") String inputOTP) throws TFException {

		boolean smsSentFlag = customerService.sendOTPToPhone(phoneNumber);
		ResponseHolder response = null;
		
		if(smsSentFlag){
			response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("OTP has been delivered to customer registed phone number : " + phoneNumber)
					.build();
		}else {
			response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("Problem occured while sending OTP to customer registed phone number : " + phoneNumber)
					.build();
		}
		
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/update-profile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateUserProfile(@RequestParam("phone-number") String phoneNumber,
			@RequestBody ProfileUpdateRequest request) throws TFException {

		User user = customerService.updateProfile(request, phoneNumber);
		
		ResponseHolder response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data(user)
					.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-foodstalls", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodStalls(@RequestParam("fcId") Long fcId){
		
		List<FoodStall> foodStalls = customerService.getFoodStalls(fcId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(foodStalls)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-foodstall-menu", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodStallMenu(@RequestParam("fs-id") Long fsId){
				
		Map<String, List<FoodItem>> foodItemsMap = customerService.getFoodItems(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(foodItemsMap)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-fooditem-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemDetails(@RequestParam("fooditem-id") Long foodItemId){
				
		FoodItem foodItem = customerService.getFoodItemDetails(foodItemId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(foodItem)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-fooditem-combination-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemCombinationDetails(@RequestParam("fooditem-id") Long foodItemId){
				
		CustomizationResponse combinationResponse = customerService.getCombinationResponse(foodItemId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(combinationResponse)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-fooditem-associated-offer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemAssociatedOffer(@RequestParam("foodItemId") Long foodItemId){
		
		Offer offer = cartService.getFoodItemAssociatedOffer(foodItemId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("OK")
				.data(offer)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
}
