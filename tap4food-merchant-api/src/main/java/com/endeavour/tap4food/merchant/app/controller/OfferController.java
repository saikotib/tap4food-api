package com.endeavour.tap4food.merchant.app.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.offer.Offer;
import com.endeavour.tap4food.app.model.offer.OfferFoodItem;
import com.endeavour.tap4food.app.request.dto.OfferFoodItemsListRequest;
import com.endeavour.tap4food.app.request.dto.OfferSuggestionItemRequest;
import com.endeavour.tap4food.app.response.dto.OfferResponse;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.merchant.app.service.OfferService;

@RestController
@RequestMapping("/api/merchant/offer")
public class OfferController {
	
	@Autowired
	private OfferService offerService;

	@RequestMapping(value = "/create-offer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createOffer(@RequestParam("fsId") Long fsId, 
			@RequestParam("requestId") String requestId,
			@RequestBody Offer offer) throws TFException{
		
		offer = offerService.createOffer(fsId, requestId, offer);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(offer)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/update-offer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateOffer(@RequestParam("fsId") Long fsId, 
			@RequestParam("offerId") Long offerId,
			@RequestBody Offer offer) throws TFException{
		
		offerService.updateOffer(fsId, offerId, offer);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(offer)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/delete-offer", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> deleteOffer(@RequestParam("fsId") Long fsId,
			@RequestParam("offerId") Long offerId) throws TFException{
		
		offerService.deleteOffer(fsId, offerId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data("Offer is deleted")
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/upload-offer-image", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadOfferImage(@RequestParam("fsId") Long fsId, 
			@RequestParam("offerId") Long offerId,
			@RequestParam("pic") MultipartFile offerImage) throws TFException, IOException{

		String cdnLink = offerService.uploadOfferImage(fsId, offerId, offerImage);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(cdnLink)
				.build();
		
		return ResponseEntity.ok().body(response);
	} 
	
	@RequestMapping(value = "/upload-offer-items-list", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadOfferItemsList(@RequestParam("fsId") Long fsId,
			@RequestParam("offerId") Long offerId,
			@RequestBody List<OfferFoodItemsListRequest> foodItemsLists) throws TFException{
		
		offerService.saveOfferFoodItemsLists(fsId, offerId, foodItemsLists);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data("Offer food items lists are saved successfully")
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/save-offer-suggestions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveOfferSuggestions(@RequestParam("fsId") Long fsId,
			@RequestParam("offerId") Long offerId,
			@RequestBody OfferSuggestionItemRequest request) throws TFException{

		offerService.saveOfferSuggestionFoodItems(fsId, offerId, request);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data("Offer suggestion items are saved successfully")
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/get-offers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOffers(@RequestParam("fsId") Long fsId) throws TFException{
		
		List<Offer> offers = offerService.getOffers(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(offers)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/get-offers-fooditems", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOfferFoodItems(@RequestParam("fsId") Long fsId) throws TFException{
		
		List<OfferFoodItem> offerFoodItems = offerService.getOfferFoodItems(fsId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(offerFoodItems)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/get-offer-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getOfferDetails(@RequestParam("fsId") Long fsId,
			@RequestParam("offerId") Long offerId) throws TFException{
		
		OfferResponse offer = offerService.getOffer(fsId, offerId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(offer)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@RequestMapping(value = "/update-offer-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateOfferStatus(@RequestParam("fsId") Long fsId,
			@RequestParam("offerId") Long offerId,
			@RequestParam("activeFlag") Boolean flag) throws TFException{
		
		String message = offerService.updateOfferStatus(fsId, offerId, flag);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data(message)
				.build();
		
		return ResponseEntity.ok().body(response);
	}
}
