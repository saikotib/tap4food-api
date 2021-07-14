package com.endeavour.tap4food.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.location.City;
import com.endeavour.tap4food.app.model.location.Country;
import com.endeavour.tap4food.app.model.location.State;
import com.endeavour.tap4food.app.repository.LocationRepository;

@Service
public class LocationService {

	@Autowired
	private LocationRepository locationRepository;
	
	public void addCountry(Country country) throws TFException {
		locationRepository.addCountry(country);
	}
	
	public void addState(String countryCode, State state) throws TFException {
		locationRepository.addState(countryCode, state);
	}
	
	public void addCity(String state, City city) throws TFException {
		locationRepository.addCity(state, city);
	}
	
	public Country getCountryInfo(String countryCode) throws TFException {
		return locationRepository.getCountryByCode(countryCode);
	}
}
