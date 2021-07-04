package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.MerchantService;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

	@Autowired
	MerchantService merchantService;

	@RequestMapping(value = "/update-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> merchantStatusUpdate(@RequestParam Long uniqueNumber, @RequestParam String status) {
		ResponseEntity<ResponseHolder> responseEntity = null;
		
		Merchant merchant = merchantService.merchantStatusUpdate(uniqueNumber, status);

		if (!Objects.isNull(merchant)) {
			
			ResponseHolder response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data(merchant)
					.build();
			
			responseEntity = ResponseEntity.ok(response);
		} else {
			
			ResponseHolder response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("No merchanat found. hence the status update not happened.")
					.build();
			
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
}
