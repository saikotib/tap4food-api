package com.endeavour.tap4food.app.response.dto;

import java.util.List;
import java.util.Map;

import com.endeavour.tap4food.app.model.offer.FoodItemsList;

import lombok.Data;

@Data
public class OfferResponse {

	private Long offerId;

	private String title;

	private String category;

	private String subCategory;

	private String cuisine;

	private Double totalPrice;

	private Double offerPrice;

	private String offerDate;

	private String offerType;

	private String offerImage;

	private Long fsId;
	
	Map<String, List<FoodItemsList>> itemsLists;
}
