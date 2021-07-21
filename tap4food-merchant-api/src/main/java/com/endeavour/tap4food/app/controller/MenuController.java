package com.endeavour.tap4food.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.response.dto.ResponseHolder;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

	@RequestMapping(value = "/create-food-item")
	public ResponseEntity<ResponseHolder> createFoodItem(){
		return null;
	}
	
	@RequestMapping(value = "/load-add-ons")
	public ResponseEntity<ResponseHolder> loadAddOns(){
		return null;
	}
}
