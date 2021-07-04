package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


	@Autowired
	AdminService adminService;
	
	@RequestMapping(value = "/update-merchant-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateMerchantStatus(@RequestParam Long merchantUniqueId, @RequestParam String status){
		
		ResponseHolder response = adminService.updateMerchantStatus(status, merchantUniqueId);
		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/create-merchant", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder>  createMerchant(@Valid @RequestBody Merchant merchant){
		
		String errorMessage = null;
		
		ResponseEntity<ResponseHolder> responseEntity = null;

		if(adminService.isMerchantFoundByEmail(merchant.getEmail())) {
			errorMessage = "Merchant email is already used.";
			
			ResponseHolder response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data(errorMessage)
					.build();
			
			responseEntity =ResponseEntity.badRequest().body(response);
			
		}else if(adminService.isMerchantFoundByPhoneNumber(merchant.getPhoneNumber())) {
			errorMessage = "Merchant phone number is already used.";
			
			ResponseHolder response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data(errorMessage)
					.build();
			
			responseEntity =ResponseEntity.badRequest().body(response);
			
		}else {
			merchant = adminService.createMerchant(merchant);
			
			if(!Objects.isNull(merchant.getId())){
				
				ResponseHolder response = ResponseHolder.builder()
						.status("success")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data(merchant)
						.build();
				
				responseEntity = ResponseEntity.ok().body(response);
				
			}else {
				
				errorMessage = "Error occurred during merchant creation";
				
				ResponseHolder response = ResponseHolder.builder()
						.status("error")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data(errorMessage)
						.build();
				
				responseEntity = ResponseEntity.ok().body(response);
				
			}
		}
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/fetch-merchants", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder>  fetchMerchants(){
		
		List<Merchant> merchants = adminService.fetchMerchants();
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(merchants)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
	}
}
