package com.endeavour.tap4food.user.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.model.offer.FoodItemsList;
import com.endeavour.tap4food.app.model.offer.Offer;
import com.endeavour.tap4food.app.response.customer.dto.OfferFoodItem;
import com.endeavour.tap4food.app.response.customer.dto.OfferListDetailsResponseDto;
import com.endeavour.tap4food.app.response.customer.dto.OfferResponseDto;
import com.endeavour.tap4food.user.app.repository.UserRepository;

@Service
public class OfferService {
	
	@Autowired
	private UserRepository userRepository;

	public List<OfferResponseDto> getOffers(Long fsId){
		
		List<OfferResponseDto> offersList = new ArrayList<OfferResponseDto>();
		
		List<Offer> offers = userRepository.getOffers(fsId);
		
		for(Offer offer : offers) {
			
			if(StringUtils.hasText(offer.getStatus()) && offer.getStatus().equalsIgnoreCase("INACTIVE")) {
				continue;
			}
			
			OfferResponseDto offerDto = new OfferResponseDto();
			offerDto.setCategory(offer.getCategory());
			offerDto.setCuisine(offer.getCuisine());
			offerDto.setFsId(fsId);
			offerDto.setOfferDate(offer.getOfferDate());
			offerDto.setOfferId(offer.getOfferId());
			offerDto.setOfferImage(offer.getOfferImage());
			offerDto.setOfferPrice(offer.getOfferPrice());
			offerDto.setOfferType(offer.getOfferType());
			offerDto.setSubCategory(offer.getSubCategory());
			offerDto.setTitle(offer.getTitle());
			offerDto.setTotalPrice(offer.getTotalPrice());
			offerDto.setOfferDescription(offer.getOfferDescription());
			
			offersList.add(offerDto);
		}
		
		return offersList;
	}
	
	public OfferListDetailsResponseDto getOfferDetails(Long offerId) {
		
		OfferListDetailsResponseDto offerListDetails = new OfferListDetailsResponseDto();
		
		List<FoodItemsList> foodItemsLists = userRepository.getFoodItemsOfOffer(offerId);
		
		Offer offer = userRepository.getOffer(offerId);
		
		offerListDetails.setOfferId(offerId);
		offerListDetails.setActualPrice(offer.getTotalPrice());
		offerListDetails.setOfferPrice(offer.getOfferPrice());
		offerListDetails.setTitle(offer.getTitle());
		offerListDetails.setOfferType(offer.getOfferType());
		
		Map<String, Map<String, List<OfferFoodItem>>> listItemsMap = new TreeMap<String, Map<String, List<OfferFoodItem>>>();
		
		Map<String, String> listDescriptionsMap = new TreeMap<String, String>();
		
		Map<String, String> buttonTypesMap = new TreeMap<String, String>();
		
		for(FoodItemsList list : foodItemsLists) {
			
			String listName = list.getListName();
			String description = list.getDescription();
			
			String itemTokens[] = list.getItemName().trim().split("-");
			
			boolean customizationFlag = true;
			
			if(list.getItemName().trim().endsWith("-")) {
				customizationFlag = false;
			}
			
			String itemName = itemTokens[0].trim();
			String combination = customizationFlag ? itemTokens[1].trim() : "";
			
			if(!listDescriptionsMap.containsKey(listName)) {
				listDescriptionsMap.put(listName, description);
			}
			
			String buttonType = list.getSelectType();
			
			if(!buttonTypesMap.containsKey(listName)) {
				buttonTypesMap.put(listName, buttonType);
			}
			
			if(buttonTypesMap.containsKey(listName)) {
				String existingButtonType = buttonTypesMap.get(listName);
				if(Objects.isNull(existingButtonType)) {
					buttonTypesMap.put(listName, "Single");
				}	
			}
			
			if(!listItemsMap.containsKey(listName)) {
				
				Map<String, List<OfferFoodItem>> itemMap = new LinkedHashMap<String, List<OfferFoodItem>>();
				
				itemMap.put(itemName, new ArrayList<OfferFoodItem>());
				
				listItemsMap.put(listName, itemMap);
			}else {
				Map<String, List<OfferFoodItem>> itemMap = listItemsMap.get(listName);
				
				if(!itemMap.containsKey(itemName)) {
					itemMap.put(itemName, new ArrayList<OfferFoodItem>());
				}
			}
			
			OfferFoodItem offerFoodItem = new OfferFoodItem();
			offerFoodItem.setActualPrice(list.getActualPrice());
			offerFoodItem.setItemName(itemName);
			offerFoodItem.setCombination(combination);
			offerFoodItem.setFoodItemId(list.getFoodItemId());
			offerFoodItem.setOfferPrice(list.getOfferPrice());
			offerFoodItem.setCustomizationFlag(customizationFlag);
			offerFoodItem.setQuantity(list.getQuantity());
			
			listItemsMap.get(listName).get(itemName).add(offerFoodItem);
		}
		
		
		
		offerListDetails.setOfferListsMap(listItemsMap);
		offerListDetails.setDescriptionsMap(listDescriptionsMap);
		offerListDetails.setButtonTypesMap(buttonTypesMap);
		
		return offerListDetails;
	}
}
