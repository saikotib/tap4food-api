package com.endeavour.tap4food.admin.app.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.admin.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.admin.app.service.MerchantService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/admin/merchant")
@Api(tags = "MerchantController", description = "MerchantController")
@CrossOrigin
public class MerchantController {

	@Autowired
	MerchantService merchantService;

	@RequestMapping(value = "/get-merchant-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getMerchantDetailsByUniqueId(@Valid @RequestParam Long uniqueNumber) {

		ResponseHolder apiResponse = merchantService.getMerchantInfo(uniqueNumber);
		
		ResponseEntity<ResponseHolder> response = new ResponseEntity<ResponseHolder>(apiResponse, HttpStatus.OK);		
		
		return response;
	}
	
}
