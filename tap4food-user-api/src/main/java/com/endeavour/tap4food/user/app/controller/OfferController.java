package com.endeavour.tap4food.user.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.response.customer.dto.OfferListDetailsResponseDto;
import com.endeavour.tap4food.app.response.customer.dto.OfferResponseDto;
import com.endeavour.tap4food.user.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.user.app.service.OfferService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/customer/offers")
@Api(tags = "OfferController", description = "OfferController")
@CrossOrigin
public class OfferController {
	
	@Autowired
	private OfferService offerService;

	@RequestMapping(value = "/get-offers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOffers(@RequestParam("fsId") Long fsId){
		
		List<OfferResponseDto> offers = offerService.getOffers(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("OK")
				.data(offers)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/get-offer-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOfferDetails(@RequestParam("offerId") Long offerId){
		
		OfferListDetailsResponseDto offerDetails = offerService.getOfferDetails(offerId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("OK")
				.data(offerDetails)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
}
