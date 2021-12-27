package com.endeavour.tap4food.admin.app.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.location.City;
import com.endeavour.tap4food.app.model.location.Country;
import com.endeavour.tap4food.app.model.location.State;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

@Repository
public class LocationRepository {

	public static final String cityCollection = MongoCollectionConstant.COLLECTION_CITY;

	public static final String countryCollection = MongoCollectionConstant.COLLECTION_COUNTRY;

	public static final String stateCollection = MongoCollectionConstant.COLLECTION_STATE;

	@Autowired
	private MongoTemplate mongoTemplate;

	public boolean isCountryExists(String name) {

		Query query = new Query(Criteria.where("name").is(name));

		Country country = mongoTemplate.findOne(query, Country.class);

		if (Objects.isNull(country))
			return false;
		else
			return true;
	}

	public boolean isStateExists(String name) {

		Query query = new Query(Criteria.where("name").is(name));

		State state = mongoTemplate.findOne(query, State.class);

		if (Objects.isNull(state))
			return false;
		else
			return true;
	}

	public boolean isCityExists(String name) {

		Query query = new Query(Criteria.where("name").is(name));

		City city = mongoTemplate.findOne(query, City.class);

		if (Objects.isNull(city))
			return false;
		else
			return true;
	}

	public void addCountry(Country country) throws TFException {

		if (isCountryExists(country.getName())) {
			throw new TFException("country already exist");
		}

		mongoTemplate.save(country);
	}
	
	public void addState(String countryCode, State state) throws TFException {

		Query countryQuery = new Query(Criteria.where("countryCode").is(countryCode));
		Country country = mongoTemplate.findOne(countryQuery, Country.class);
		
		if(Objects.isNull(country)) {
			throw new TFException("Invalid country. hence state can't be added");
		}
		
		if (isStateExists(state.getName())) {
			throw new TFException("State already exist");
		}
		
		state.setCountryCode(countryCode);

		mongoTemplate.save(state);
		
	}
	
	public void addCity(String stateName, City city) throws TFException {

		if (isCityExists(city.getName())) {
			throw new TFException("city already exist");
		}
		
		Query stateQuery = new Query(Criteria.where("name").is(stateName));
		State state = mongoTemplate.findOne(stateQuery, State.class);
		
		if(Objects.isNull(state)) {
			throw new TFException("invalid state. hence city can't be added");
		}
		
		city.setState(stateName);

		mongoTemplate.save(city);
		
	}
	
	public Country getCountryByCode(String countryCode) throws TFException {
		
		Query query = new Query(Criteria.where("countryCode").is(countryCode));
		
		Country country = mongoTemplate.findOne(query, Country.class);
		
		if(Objects.isNull(country)) {
			throw new TFException("Invalid country code");
		}
		
		return country;
	}
	
	public List<Country> getCountries() throws TFException {
		
		List<Country> countries = mongoTemplate.findAll(Country.class);
		return countries;
	}
	
	public List<State> getStates(String countryCode) throws TFException {
		
		Query query = new Query(Criteria.where("countryCode").is(countryCode));
		
		List<State> states = mongoTemplate.find(query, State.class);
		
		return states;
	}
	
	public List<City> getCities(String state) throws TFException {
		
		Query query = new Query(Criteria.where("state").is(state));
		
		List<City> cities = mongoTemplate.find(query, City.class);
		
		return cities;
	}
}
