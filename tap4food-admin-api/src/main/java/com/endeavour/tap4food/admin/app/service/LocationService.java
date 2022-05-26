package com.endeavour.tap4food.admin.app.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.admin.app.repository.LocationRepository;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.location.City;
import com.endeavour.tap4food.app.model.location.Country;
import com.endeavour.tap4food.app.model.location.State;

@Service
public class LocationService {

	@Autowired
	private LocationRepository locationRepository;
	
	public void addCountry(Country country) throws TFException {
		locationRepository.addCountry(country);
	}
	
	public void updateCountry(Country country) throws TFException {
		
		Country existingCountry = locationRepository.getCountry(country.getId());
		
		if(Objects.nonNull(existingCountry)) {
			existingCountry.setName(country.getName());
			locationRepository.updateCountry(country);
		}		
	}
	
	public void addState(String countryCode, State state) throws TFException {
		locationRepository.addState(countryCode, state);
	}
	
	public void updateState(State state) throws TFException {
		
		State existingState = locationRepository.getState(state.getId());
		if(Objects.nonNull(existingState)) {
			existingState.setName(state.getName());
			locationRepository.updateState(existingState);
		}
	}
	
	public void addCity(String state, City city) throws TFException {
		locationRepository.addCity(state, city);
	}
	
	public void updateCity(City city) throws TFException {
		
		City existingCity = locationRepository.getCity(city.getId());
		if(Objects.nonNull(existingCity)) {
			existingCity.setName(city.getName());
			locationRepository.updateCity(existingCity);
		}
	}
	
	public Country getCountryInfo(String countryCode) throws TFException {
		return locationRepository.getCountryByCode(countryCode);
	}
	
	public List<Country> getCountries() throws TFException {
		return locationRepository.getCountries();
	}
	
	public List<State> getStates() throws TFException {
		return locationRepository.getStates();
	}
	
	public List<State> getStates(String countryCode) throws TFException {
		return locationRepository.getStates(countryCode);
	}
	
	public List<City> getCities(String state) throws TFException {
		return locationRepository.getCities(state);
	}
	
	public List<City> getCities() throws TFException {
		return locationRepository.getCities();
	}
}
