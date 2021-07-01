package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.AdminService;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


	@Autowired
	AdminService adminService;
	
	@RequestMapping(value = "/create-merchant", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?>  createMerchant(@Valid @RequestBody Merchant merchant){
		merchant.setCreatedBy("Admin");
		boolean createMerchantResFlag = adminService.createMerchant(merchant);
		ResponseEntity response = null;
		
		if(createMerchantResFlag){
			response =ResponseEntity.ok( ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("Merchant details saved successfully")
					.build());
		}else {
			response =  ResponseEntity.badRequest().body("Error occurred");
		}
		
		return response;
		
	}
}
