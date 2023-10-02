package com.endeavour.tap4food.user.app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.ContactUs;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemDto;
import com.endeavour.tap4food.app.model.offer.Offer;
import com.endeavour.tap4food.app.response.dto.CustomizationResponse;
import com.endeavour.tap4food.app.response.dto.FoodCourtResponse;
import com.endeavour.tap4food.user.app.payload.request.ProfileUpdateRequest;
import com.endeavour.tap4food.user.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.user.app.security.model.User;
import com.endeavour.tap4food.user.app.service.CartService;
import com.endeavour.tap4food.user.app.service.CustomerService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/customer")
@Api(tags = "CustomerController", description = "All user operations are available here")
@CrossOrigin
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CartService cartService;

	@RequestMapping(value = "/view-otp", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> login(@RequestParam("phone-number") String phoneNumber) {

		Otp otp = customerService.fetchOtp(phoneNumber);
		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(otp).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/verify-otp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> verifyOtp(@RequestParam("phone-number") String phoneNumber,
			@RequestParam("input-otp") String inputOTP) throws TFException {

		boolean smsSentFlag = customerService.sendOTPToPhone(phoneNumber);
		ResponseHolder response = null;

		if (smsSentFlag) {
			response = ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
					.data("OTP has been delivered to customer registed phone number : " + phoneNumber).build();
		} else {
			response = ResponseHolder.builder().status("error").timestamp(String.valueOf(LocalDateTime.now()))
					.data("Problem occured while sending OTP to customer registed phone number : " + phoneNumber)
					.build();
		}

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/update-profile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateUserProfile(@RequestParam("phone-number") String phoneNumber,
			@RequestBody ProfileUpdateRequest request) throws TFException {

		User user = customerService.updateProfile(request, phoneNumber);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(user).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-foodstalls", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodStalls(@RequestParam("fcId") Long fcId,
			@RequestParam(value = "timezone", required = false) String timezone) {

		if (StringUtils.hasText(timezone)) {
			timezone = timezone.replaceAll("_", "/");
		} else {
			timezone = "Asia/Calcutta";
		}

		System.out.println("Timezone" + timezone);

		List<FoodStall> foodStalls = customerService.getFoodStalls(fcId, timezone);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(foodStalls).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-userdata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getUserData(@RequestParam("phoneNumber") String phoneNumber)
			throws TFException {

		User userData = customerService.getUserData(phoneNumber);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(userData).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-foodcourt-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodCourtDetails(@RequestParam("fcId") Long fcId) {

		FoodCourtResponse foodCourt = customerService.getFoodCourtDetails(fcId);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(foodCourt).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-foodstall-menu", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodStallMenu(@RequestParam("fs-id") Long fsId) {

//		Map<String, List<FoodItem>> foodItemsMap = customerService.getFoodItems(fsId);
		Map<String, List<FoodItemDto>> foodItemsMap = customerService.getFoodItemsMapped(fsId);
		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(foodItemsMap).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-foodstall", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodStall(@RequestParam("fs-id") Long fsId,
			@RequestParam(value = "timezone", required = false) String timezone) {

		if (StringUtils.hasText(timezone)) {
			timezone = timezone.replaceAll("_", "/");
		} else {
			timezone = "Asia/Calcutta";
		}

		System.out.println("Timezone" + timezone);

		FoodStall stall = customerService.getFoodStall(fsId, timezone);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(stall).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-fooditem-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemDetails(@RequestParam("fooditem-id") Long foodItemId) {

		FoodItem foodItem = customerService.getFoodItemDetails(foodItemId);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(foodItem).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-suggestion-items", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemSuggestions(@RequestBody Set<Long> foodItemIdSet) {

		List<FoodItem> foodItems = customerService.getFoodItemSuggesions(foodItemIdSet);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(foodItems).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-fooditem-combination-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemCombinationDetails(@RequestParam("fooditem-id") Long foodItemId) {

		CustomizationResponse combinationResponse = customerService.getCombinationResponse(foodItemId);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(combinationResponse).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-fooditem-associated-offer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemAssociatedOffer(@RequestParam("foodItemId") Long foodItemId) {

		Offer offer = cartService.getFoodItemAssociatedOffer(foodItemId);

		ResponseHolder response = ResponseHolder.builder().status("OK").data(offer).build();

		return ResponseEntity.ok().body(response);
	}

	@RequestMapping(value = "/submitContactUsForm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> submitContactUsForm(@RequestBody ContactUs form) {

		customerService.submitContactUsForm(form);

		ResponseHolder response = ResponseHolder.builder().status("OK").data("Details are submitted").build();

		return ResponseEntity.ok().body(response);
	}

	@RequestMapping(value = "/getAboutData", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getAboutData() {

		ResponseHolder response = ResponseHolder.builder().status("OK").data(customerService.getAboutData()).build();

		return ResponseEntity.ok().body(response);
	}

	@RequestMapping(value = "/getTnC", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getTnC() {

		ResponseHolder response = ResponseHolder.builder().status("OK").data(customerService.getTnC()).build();

		return ResponseEntity.ok().body(response);
	}

	@RequestMapping(value = "/get-autosugestion-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getSuggestionList(@RequestParam("fc-id") Long fcId) {

		Set<String> suggestions = customerService.getSuggestionList(fcId);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(suggestions).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/image/{foodItemId}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getImage(@PathVariable Long foodItemId) {
		List<Binary> imageList = customerService.getImageBytesById(foodItemId);

		if (imageList != null && !imageList.isEmpty()) {
			Binary imageBinary = imageList.get(0); // Assuming you're interested in the first binary

			if (imageBinary != null) {
				byte[] imageBytes = imageBinary.getData(); // Use the appropriate method to get image bytes

				if (imageBytes != null && imageBytes.length > 0) {
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.IMAGE_JPEG);
					return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
				}
			}
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

}
