package com.endeavour.tap4food.app.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.fooditem.AddOns;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomizationPricing;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.response.dto.FoodItemResponse;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.FoodItemService;
import com.endeavour.tap4food.app.util.ImageConstants;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/menu")
@Api(tags = "MenuController", description = "MenuController")
public class MenuController {
	
	@Autowired
	private FoodItemService foodItemService;

	@RequestMapping(value = "/create-food-item", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createFoodItem(@RequestParam("merchant-id") Long merchantId,
			@RequestParam("fs-id") Long fsId,
			@RequestParam("request-id") String requestId,
			@RequestBody FoodItem foodItem) throws TFException{
		
		foodItem.setRequestId(requestId);
		foodItem.setTotalReviews(1);
		
		foodItemService.addFoodItem(merchantId, fsId, foodItem);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data("Food item is created succesfully")
				.build();
		
		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/upload-food-item-pics", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadFoodItemPics(@RequestParam("fs-id") Long fsId,
			@RequestParam("request-id") String requestId, 
			@RequestParam(value = "pic", required = true) List<MultipartFile> foodItemPics) throws TFException {

		ResponseEntity<ResponseHolder> response = null;

		for(MultipartFile pic : foodItemPics) {
			System.out.println(pic.getSize());
			String picType = pic.getOriginalFilename().split("\\.")[1].toLowerCase();
			System.out.println(picType);
			if (!Arrays.asList(ImageConstants.IMAGE_JPEG, ImageConstants.IMAGE_PNG, ImageConstants.IMAGE_JPG)
					.contains(picType)) {

				throw new TFException("File must be an Image");
			}
		}
			
		FoodItem foodItem = foodItemService.uploadFoodItemPics(fsId, requestId, foodItemPics);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.status("success")
				.data(foodItem)
				.build();
		
		response = ResponseEntity.ok().body(responseHolder);
		
		return response;
	}
	
	@RequestMapping(value = "/get-food-items", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItems(@RequestParam("fs-id") Long fsId){
		
		List<FoodItemResponse> foodItems = foodItemService.getFoodItems(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(foodItems)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-food-items-for-offers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemsForOffers(@RequestParam("fs-id") Long fsId){
		
		List<FoodItemResponse> foodItems = foodItemService.getFoodItemsForOffers(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(foodItems)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-fooditems-pricing-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemPricingInfo(@RequestParam("fs-id") Long fsId){
		
		List<FoodItemPricing> foodItems = foodItemService.getFoodItemPricingDetails(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(foodItems)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/update-fooditem-price", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodItemPrice(@RequestParam("fs-id") Long fsId, @RequestParam("pricing-id") String pricingId, @RequestParam("price") Double price) throws TFException{
		
		FoodItemPricing foodItemPricing = foodItemService.updateFoodItemPrice(fsId, pricingId, price);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(foodItemPricing)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-fooditems-customizing-pricing-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemCustomizingPricingInfo(@RequestParam("fs-id") Long fsId){
		
		List<FoodItemCustomizationPricing> foodItems = foodItemService.getFoodItemCustomizationPricingDetails(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(foodItems)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-fooditem-customizing-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodItemCustomizingPricingInfo(@RequestParam("fs-id") Long fsId, @RequestParam("foodItemId") Long foodItemId){
		
		List<FoodItemCustomizationPricing> foodItems = foodItemService.getFoodItemCustomizationPricingDetails(fsId, foodItemId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(foodItems)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/update-fooditem-customization-price", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodItemCustomizationPrice(@RequestParam("fs-id") Long fsId, @RequestParam("pricing-id") String pricingId, @RequestParam("price") Double price){
		
		FoodItemCustomizationPricing foodItemPricing = foodItemService.updateFoodItemCustomizationPrice(fsId, pricingId, price);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(foodItemPricing)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/load-add-ons", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> loadAddOns(@RequestParam("merchant-id") Long merchantId,
			@RequestParam("fs-id") Long fsId){
		
		List<AddOns> addOns = foodItemService.getAddOns(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(addOns)
				.build();		
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/add-customised-food-items", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addFoodItemCustomiseDetails(@RequestParam("merchant-id") Long merchantId,
			@RequestParam("fs-id") Long fsId,
			@RequestParam("request-id") String requestId,
			@RequestBody FoodItemCustomiseDetails foodItemCustomiseDetails) throws TFException{
		
		foodItemService.addFoodItemCustomiseDetails(requestId, foodItemCustomiseDetails);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data("Customisation details are saved")
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	
}
