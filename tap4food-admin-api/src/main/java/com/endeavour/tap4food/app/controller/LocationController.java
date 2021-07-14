package com.endeavour.tap4food.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.location.City;
import com.endeavour.tap4food.app.model.location.Country;
import com.endeavour.tap4food.app.model.location.State;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.LocationService;

@RestController
@RequestMapping("/api/admin/location")
public class LocationController {

	@Autowired
	private LocationService locationService;
	
	@RequestMapping(value = "/add-country", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addCountry(@RequestBody Country country) throws TFException{
		
		if(!StringUtils.hasText(country.getCountryCode())){
			throw new TFException("Country code is mandatory");
		}
		
		locationService.addCountry(country);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data("country is added successfully")
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/add-state", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addState(@RequestParam("country-code") String countryCode, @RequestBody State state) throws TFException{
		
		if(!StringUtils.hasText(countryCode)){
			throw new TFException("Invalid country code");
		}
		
		if(!StringUtils.hasText(state.getName())){
			throw new TFException("Name of the state is invalid");
		}
		
		locationService.addState(countryCode, state);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data("state is added successfully")
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/add-city", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addState(@RequestParam("state-name") String stateName, @RequestBody City city) throws TFException{
		
		if(!StringUtils.hasText(stateName)){
			throw new TFException("Invalid state name");
		}
		
		if(!StringUtils.hasText(city.getName())){
			throw new TFException("Name of the city is invalid");
		}
		
		locationService.addCity(stateName, city);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data("city is added successfully")
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get-country-info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getCountryInfo(@RequestParam("country-code") String countryCode) throws TFException{
		
		if(!StringUtils.hasText(countryCode)){
			throw new TFException("Invalid country code");
		}
			
		Country country = locationService.getCountryInfo(countryCode);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(country)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
}
