package com.endeavour.tap4food.app.response.customer.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OfferListDetailsResponseDto {
	
	private Long offerId;
	
	private String title;
	
	private String description;
	
	private Double actualPrice;
	
	private Double offerPrice;
	
	private String offerType;

	@JsonProperty("offerLists")
	private Map<String, Map<String, List<OfferFoodItem>>> offerListsMap;
	
	@JsonProperty("descriptions")
	private Map<String, String> descriptionsMap;
	
	@JsonProperty("buttonTypes")
	private Map<String, String> buttonTypesMap;
	
}
