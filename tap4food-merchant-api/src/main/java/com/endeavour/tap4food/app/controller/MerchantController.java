package com.endeavour.tap4food.app.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.MerchantBankDetails;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.MerchantService;
import com.endeavour.tap4food.app.util.AvatarImage;
import com.endeavour.tap4food.app.util.ImageConstants;
/*import org.apache.http.entity.ContentType;*/

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

	@Autowired
	MerchantService merchantService;

	@RequestMapping(value = "/update-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> merchantStatusUpdate(@RequestParam Long uniqueNumber,
			@RequestParam String status) {

		ResponseEntity<ResponseHolder> responseEntity = null;

		Merchant merchant = merchantService.merchantStatusUpdate(uniqueNumber, status);

		if (!Objects.isNull(merchant)) {

			ResponseHolder response = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchant).build();

			responseEntity = ResponseEntity.ok(response);
		} else {

			ResponseHolder response = ResponseHolder.builder().status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("No merchanat found. hence the status update not happened.").build();

			responseEntity = ResponseEntity.badRequest().body(response);
		}

		return responseEntity;
	}

	@RequestMapping(value = "/create-merchant", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createMerchant(@Valid @RequestBody Merchant merchant) {

		/* merchant.setCreatedBy("Admin"); */
		try {
			merchant.setPersonalIdCard(new Binary(BsonBinarySubType.BINARY,(new AvatarImage()).avatarImage()));
			merchant.setProfilePic(new Binary(BsonBinarySubType.BINARY,(new AvatarImage()).avatarImage()));
		} catch (IOException e) {

		}

		boolean createMerchantResFlag = merchantService.createMerchant(merchant);
		ResponseEntity response = null;

		if (createMerchantResFlag) {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Merchant details saved successfully").build());
		} else {
			response = ResponseEntity.badRequest().body("Error occurred");
		}

		return response;

	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity<?> updateMerchant(@Valid @RequestBody Merchant merchant) {
		ResponseEntity response = null;

		Optional<Merchant> merchantRes = merchantService.updateMerchant(merchant);
		if (merchantRes.isPresent()) {
			response = ResponseEntity.ok(merchantRes);
		} else {
			response = ResponseEntity.badRequest().body("No data found");
		}

		return response;
	}

	@RequestMapping(path = "/add-category", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createMenuCategory(@Valid @RequestBody Category menuCategory) {

		merchantService.createMenuCategory(menuCategory);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(menuCategory).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(path = "/add-subcategory", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createMenuSubCategory(@Valid @RequestBody SubCategory menuSubCategories) {

		merchantService.createMenuSubCategory(menuSubCategories);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(menuSubCategories).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/fetch-categories", method = RequestMethod.GET)
	public ResponseEntity<ResponseHolder> getAllCategories() {
		List<Category> categoryName = merchantService.getAllCategories();
		ResponseHolder response = ResponseHolder.builder().status("Done").timestamp(String.valueOf(LocalDateTime.now()))
				.data(categoryName).build();
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/{category-id}/fetch-sub-categories", method = RequestMethod.GET)
	public ResponseEntity<ResponseHolder> getAllSubCategories(@Valid @PathVariable("category-id") String id) {
		Set<SubCategory> subCategories = merchantService.getAllSubCategories(id);
		ResponseHolder response = ResponseHolder.builder().status("Done").timestamp(String.valueOf(LocalDateTime.now()))
				.data(subCategories).build();
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/{merchant-id}/upload-pic", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadProfilePic(@Valid @PathVariable("merchant-id") Long id,
			@RequestParam(value = "pic", required = true) MultipartFile pic,
			@RequestParam(required = true) String type) {

		ResponseEntity<ResponseHolder> response = null;
		Optional<Merchant> merchantResponse = null;

		System.out.println(pic.getSize());
		String picType = pic.getOriginalFilename().split("\\.")[1].toLowerCase();
		System.out.println(picType);
		if (!Arrays.asList(ImageConstants.IMAGE_JPEG, ImageConstants.IMAGE_PNG, ImageConstants.IMAGE_JPG)
				.contains(picType)) {
			System.out.println("inf");
			throw new IllegalStateException("File must be an Image");
		} else {
			System.out.println("else");
			merchantResponse = merchantService.uploadProfilePic(id, pic, type);
		}

		if (merchantResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status(type + " succesfully uploaded")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantResponse.get()).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while uploading " + type)
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantResponse).build());

		} 
		
		

		return response;
	}

	@RequestMapping(value = "/{merchant-unique-number}/add-bank-details", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addBankDetails(@Valid @PathVariable("merchant-unique-number") Long uniqueId,
			@RequestBody MerchantBankDetails merchantBankDetails) {

		Optional<Merchant> merchantBankDetailsResponse = merchantService.saveMerchantBankDetails(uniqueId,
				merchantBankDetails);
		ResponseEntity<ResponseHolder> response = null;

		if (Objects.nonNull(merchantBankDetailsResponse.get().getBankDetails())) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Merchant Bank Details saved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantBankDetailsResponse.get()).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while saving Merchant Bank Details")
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantBankDetailsResponse).build());

		}
		return response;
	}

	@RequestMapping(value = "/{merchant-unique-number}/add-foodstall-timings", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveFoodStallTimings(
			@Valid @PathVariable("merchant-unique-number") Long uniqueId, @RequestBody ArrayList<WeekDay> weekDay) {

		Optional<FoodStallTimings> merchantFoodStallTimingsResponse = merchantService.saveFoodCourtTimings(uniqueId,
				weekDay);
		ResponseEntity<ResponseHolder> response = null;

		if (merchantFoodStallTimingsResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Stall Timings saved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantFoodStallTimingsResponse.get())
					.build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while saving Food Stall Timings")
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantFoodStallTimingsResponse.get())
							.build());

		}
		return response;
	}

	@RequestMapping(value = "/{merchant-unique-number}/update-bank-details", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateBankDetails(
			@Valid @PathVariable("merchant-unique-number") Long uniqueId,
			@RequestBody MerchantBankDetails merchantBankDetails) {

		Optional<Merchant> merchantBankDetailsResponse = merchantService.saveMerchantBankDetails(uniqueId,
				merchantBankDetails);
		ResponseEntity<ResponseHolder> response = null;

		if (merchantBankDetailsResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Merchant Bank Details updated succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantBankDetailsResponse.get()).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while updating Merchant Bank Details")
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantBankDetailsResponse).build());

		}
		return response;
	}

	@RequestMapping(value = "/{food-court-unique-number}/update-foodstall-timings", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodStallTimings(
			@Valid @PathVariable("food-court-unique-number") String uniqueId, @RequestBody ArrayList<WeekDay> weekDay) {

		Collection<WeekDay> merchantFoodStallTimingsResponse = merchantService.updateFoodCourtTimings(uniqueId,
				weekDay);
		ResponseEntity<ResponseHolder> response = null;

		if (!ObjectUtils.isEmpty(merchantFoodStallTimingsResponse)) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Stall Timings saved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantFoodStallTimingsResponse).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while saving Food Stall Timings")
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantFoodStallTimingsResponse)
							.build());
		}
		return response;
	}

	@RequestMapping(value = "/{food-court-unique-number}/get-foodstall-timings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodStallTimingsByUniqueId(
			@Valid @PathVariable("food-court-unique-number") String uniqueId) {

		List<WeekDay> weekDayRes = merchantService.getFoodCourtTimingsByUniqueId(uniqueId);
		ResponseEntity<ResponseHolder> response = null;

		if (!ObjectUtils.isEmpty(weekDayRes)) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Stall Timings retrieved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(weekDayRes).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while retrieving Food Stall Timings")
							.timestamp(String.valueOf(LocalDateTime.now())).data(weekDayRes).build());

		}
		return response;
	}

	@RequestMapping(value = "/{merchant-unique-number}/get-bank-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getBankDetailsByUniqueId(
			@Valid @PathVariable("merchant-unique-number") Long uniqueId) {

		Optional<MerchantBankDetails> merchantBankDetailsResponse = merchantService.getBankDetailsByUniqueId(uniqueId);
		ResponseEntity<ResponseHolder> response = null;

		if (merchantBankDetailsResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Merchant Bank Details retrieved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantBankDetailsResponse.get()).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while retrieving Merchant Bank Details")
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantBankDetailsResponse).build());

		}
		return response;
	}

	@RequestMapping(value = "/get-merchant-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getMerchantDetailsByUniqueId(@Valid @RequestParam Long uniqueNumber) {

		Optional<Merchant> merchantDetailsResponse = merchantService.getMerchantDetailsByUniqueId(uniqueNumber);
		ResponseEntity<ResponseHolder> response = null;

		if (merchantDetailsResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Merchant Details retrieved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantDetailsResponse.get()).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while retrieving Merchant Details")
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantDetailsResponse).build());

		}
		return response;
	}
	
	
	
	
	@RequestMapping(value = "/{merchant-id}/delete-pic", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> deleteProfilePic(@Valid @PathVariable("merchant-id") Long id,
			@RequestParam(required = true) String type) {

		ResponseEntity<ResponseHolder> response = null;
		Optional<Merchant> merchantResponse = null;

			merchantResponse = merchantService.deleteProfilePic(id,type);

		if (merchantResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status(type + " succesfully deleted")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantResponse.get()).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while uploading " + type)
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantResponse).build());

		}

		return response;
	}


}
