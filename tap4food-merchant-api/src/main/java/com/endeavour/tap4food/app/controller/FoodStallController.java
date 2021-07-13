package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.FoodStallService;

@RestController
@RequestMapping("/api/foodstall")
public class FoodStallController {

	@Autowired
	private FoodStallService foodStallService;

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createFoodStall(@RequestParam("merchant-number") Long merchantId,
			@RequestBody FoodStall foodStall) throws TFException {

		foodStallService.createFoodStall(merchantId, foodStall);

		ResponseHolder response = ResponseHolder.builder().data(foodStall).status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}
}
