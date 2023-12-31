package com.endeavour.tap4food.merchant.app.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.MerchantBankDetails;
import com.endeavour.tap4food.app.model.MerchantContactAdmin;
import com.endeavour.tap4food.app.model.MerchantSettings;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.response.dto.StallManager;
import com.endeavour.tap4food.app.util.ImageConstants;
/*import org.apache.http.entity.ContentType;*/
import com.endeavour.tap4food.merchant.app.service.MerchantService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/merchant")
@Api(tags = "MerchantController", description = "MerchantController")
public class MerchantController {

	@Autowired
	MerchantService merchantService;

	@RequestMapping(value = "/update-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> merchantStatusUpdate(@RequestParam Long uniqueNumber,
			@RequestParam String status) throws TFException {

		ResponseEntity<ResponseHolder> responseEntity = null;

		Merchant merchant = merchantService.updateMerchantStatus(uniqueNumber, status);

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
	public ResponseEntity<ResponseHolder> createMerchant(@Valid @RequestBody Merchant merchant, 
			@RequestParam(value = "stall-manager-creation-flag", required = false) boolean stallManagerCreationFlag,
			@RequestParam(value = "parent-merchant", required = false) Long parentMerchantId, 
			@RequestParam(value = "fs-id", required = false) Long fsId) throws TFException {

		ResponseEntity response = null;
		
		if(stallManagerCreationFlag) {
			
			if(Objects.isNull(parentMerchantId)) {
				throw new TFException("Invalid maerchant/owner ID");
			}
			
			if(Objects.isNull(fsId)) {
				throw new TFException("Invalid Foodstall ID");
			}
			
			merchant = merchantService.createStallManager(merchant, fsId, parentMerchantId);
			
			if (Objects.nonNull(merchant)) {
				response = ResponseEntity
						.ok(ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
								.data(merchant).build());
			} else {
				response = ResponseEntity.badRequest().body("Error occurred");
			}
		}else {
			merchant = merchantService.createMerchant(merchant);
			
			if (Objects.nonNull(merchant)) {
				response = ResponseEntity
						.ok(ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
								.data(merchant).build());
			} else {
				response = ResponseEntity.badRequest().body("Error occurred");
			}
		}
		

		return response;

	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateMerchant(@Valid @RequestBody Merchant merchant) {
		ResponseEntity response = null;

		merchant = merchantService.updateMerchant(merchant);
		if (!Objects.isNull(merchant)) {
			response = ResponseEntity.ok(merchant);
		} else {
			response = ResponseEntity.badRequest().body("Merchant is not available");
		}

		return response;
	}
	
	@RequestMapping(value = "/{merchant-id}/upload-pic", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadPic(@Valid @PathVariable("merchant-id") Long id,
			@RequestParam(value = "pic", required = true) MultipartFile pic,
			@RequestParam(required = true) String type) throws TFException {

		ResponseEntity<ResponseHolder> response = null;
		Optional<Merchant> merchantResponse = null;

		System.out.println(pic.getSize());
		String picType = pic.getOriginalFilename().split("\\.")[1].toLowerCase();
		System.out.println(picType);
		if (!Arrays.asList(ImageConstants.IMAGE_JPEG, ImageConstants.IMAGE_PNG, ImageConstants.IMAGE_JPG)
				.contains(picType)) {
			System.out.println("inf");
			throw new TFException("File must be an Image");
		} else {
			System.out.println("else");
			merchantResponse = merchantService.uploadProfilePic(id, pic, type);
		}

		if (merchantResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status(type + " succesfully uploaded")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantResponse.get()).build());
		} else {
			throw new TFException("Error occurred while uploading " + type);
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
	public ResponseEntity<ResponseHolder> getMerchantDetailsByUniqueId(@Valid @RequestParam Long uniqueNumber) throws TFException {

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
	
	@RequestMapping(value = "/get-stall-managers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getStallManagers(@RequestParam("parent-merchant") Long parentMerchantId) throws TFException {

		List<StallManager> stallManagers = merchantService.getStallManagers(parentMerchantId);
		ResponseEntity<ResponseHolder> response = null;

		response = ResponseEntity.ok(ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(stallManagers).build());

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
	
	@RequestMapping(value = "/get-business-units", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getBusinessUnits(@RequestParam("country") String country, 
			@RequestParam("state") String state,
			@RequestParam("city") String city){
		
		List<BusinessUnit> businessUnits = merchantService.getBusinessUnits(country, state, city);
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(businessUnits)
				.build();
		
		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/get-foodcourts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodCourts(@RequestParam("buId") Long buId ){
		List<FoodCourt> foodCourts = merchantService.getFoodcourts(buId);
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(foodCourts)
				.build();
		
		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		return responseEntity;
	}

	@RequestMapping(value = "/writeToAdmin", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> writeToAdmin(@RequestBody MerchantContactAdmin request){
		
		merchantService.saveMerchantMessageToAdmin(request);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data("Message is saved.")
				.build();
		
		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/saveSettings", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveSettings(@RequestBody MerchantSettings request, @RequestParam("key") String key){
		
		merchantService.saveSettings(request.getMerchantId(), key);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data("Settings are saved.")
				.build();
		
		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		return responseEntity;
	} 
	
	@RequestMapping(value = "/{merchantId}/getSettings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getSettings(@PathVariable("merchantId") Long merchantId){
		
		MerchantSettings settings = merchantService.getSettings(merchantId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(settings)
				.build();
		
		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		return responseEntity;
	} 

}
