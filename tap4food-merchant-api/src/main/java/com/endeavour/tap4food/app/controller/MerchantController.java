package com.endeavour.tap4food.app.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
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

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.MerchantBankDetails;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.MerchantService;
import com.endeavour.tap4food.app.util.AvatarImage;
import com.endeavour.tap4food.app.util.ImageConstants;
/*import org.apache.http.entity.ContentType;*/

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/merchant")
@Api(tags = "MerchantController", description = "MerchantController")
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
	public ResponseEntity<?> createMerchant(@Valid @RequestBody Merchant merchant, 
			@RequestParam(value = "childMerchantCreationFlag", required = false) boolean childMerchantCreationFlag,
			@RequestParam(value = "parent-merchant", required = false) Long parentMerchantId) {

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
		
		//test
		

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
