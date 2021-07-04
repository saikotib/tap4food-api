package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

import com.endeavour.tap4food.app.model.MenuCategory;
import com.endeavour.tap4food.app.model.MenuSubCategory;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.MerchantService;
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
	public ResponseEntity<ResponseHolder> createMenuCategory(@Valid @RequestBody MenuCategory menuCategory) {

		merchantService.createMenuCategory(menuCategory);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(menuCategory).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(path = "/add-subcategory", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createMenuSubCategory(@Valid @RequestBody MenuSubCategory menuSubCategories) {

		merchantService.createMenuSubCategory(menuSubCategories);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(menuSubCategories).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/fetch-categories", method = RequestMethod.GET)
	public ResponseEntity<ResponseHolder> getAllCategories() {
		List<MenuCategory> categoryName = merchantService.getAllCategories();
		ResponseHolder response = ResponseHolder.builder().status("Done").timestamp(String.valueOf(LocalDateTime.now()))
				.data(categoryName).build();
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/{category-id}/fetch-sub-categories", method = RequestMethod.GET)
	public ResponseEntity<ResponseHolder> getAllSubCategories(@Valid @PathVariable("category-id") String id) {
		Set<MenuSubCategory> subCategories = merchantService.getAllSubCategories(id);
		ResponseHolder response = ResponseHolder.builder().status("Done").timestamp(String.valueOf(LocalDateTime.now()))
				.data(subCategories).build();
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/{merchant-id}/upload-pic", method = RequestMethod.POST)
	public ResponseEntity<ResponseHolder> uploadProdilePic(@Valid @PathVariable("merchant-id") String id,
			@RequestParam(value = "pic", required = true) MultipartFile pic,@RequestParam(required = true) String type) {

		ResponseEntity<ResponseHolder> response = null;
		Optional<Merchant> merchantResponse = null;

		System.out.println(pic.getSize());
		String picType = pic.getOriginalFilename().split("\\.")[1];
		if (!Arrays.asList(ImageConstants.IMAGE_JPEG, ImageConstants.IMAGE_PNG, ImageConstants.IMAGE_JPG)
				.contains(picType)) {
			throw new IllegalStateException("File must be an Image");
		} else {
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
}
