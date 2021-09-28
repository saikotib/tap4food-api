package com.endeavour.tap4food.app.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.offer.FoodItemsList;
import com.endeavour.tap4food.app.model.offer.Offer;
import com.endeavour.tap4food.app.model.offer.SuggestionItem;
import com.endeavour.tap4food.app.repository.FoodStallRepository;
import com.endeavour.tap4food.app.repository.OfferRepository;
import com.endeavour.tap4food.app.request.dto.OfferFoodItemsListRequest;
import com.endeavour.tap4food.app.request.dto.OfferFoodItemsListRequest.Item;
import com.endeavour.tap4food.app.request.dto.OfferSuggestionItemRequest;
import com.endeavour.tap4food.app.request.dto.OfferSuggestionItemRequest.SuggestItem;
import com.endeavour.tap4food.app.response.dto.OfferResponse;
import com.endeavour.tap4food.app.util.MediaConstants;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OfferService {

	@Autowired
	private OfferRepository offerRepository;
	
	@Autowired
	private FoodStallRepository foodStallRepository;
	
	@Autowired
	private CommonSequenceService commonSequenceService;
	
	@Autowired
	private CommonService commonService;
	
	@Value("${images.server}")
	private String mediaServerUrl;
	
	public Offer createOffer(Long fsId, String requestId, Offer offer) throws TFException {
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Foodstall is not found");
		}
		
		offer.setRequestId(requestId);
		offer.setOfferId(getIdForNewOffer());
		offer.setFsId(fsId);
		
		offer = offerRepository.createOffer(offer);
		
		return offer;
	}
	
	public String uploadOfferImage(Long fsId, Long offerId, MultipartFile inputImage) throws TFException, IOException {
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Foodstall is not found");
		}
		
		String fileName = "offer_" + offerId + "_" + inputImage.getOriginalFilename();
		
		String uploadPath = commonService.getMerhantMediaDirs().get(MediaConstants.GET_KEY_OFFER_PIC_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(foodStall.getMerchantId())).replaceAll(MediaConstants.IDENTIFIER_FSID, String.valueOf(foodStall.getFoodStallId()));
		
		new File(uploadPath).mkdirs();
		
		Path path = Paths.get(uploadPath);
		
		File existingFile = new File(uploadPath + File.separator + fileName);
		
		if(existingFile.exists()) {
			if(existingFile.delete()) {
				log.info("Deleted the existing file");
			}
		}
		
		Files.copy(inputImage.getInputStream(), path.resolve(fileName));
		
		log.info("Offer Image Path : " + uploadPath);
		log.info("Offer Image Name : " + fileName);
		
		log.info("Is Base Loc found :" + uploadPath.contains(commonService.getMediaBaseLocation()));
		
		String picLink = uploadPath.replaceAll(commonService.getMediaBaseLocation(), "").replaceAll("\\\\", "/");

		picLink = mediaServerUrl + picLink + "/" + fileName;
		
		log.info("OfferImage CDN Link :" + picLink);
		
		return picLink;
	}
	
	public List<Offer> getOffers(Long fsId) throws TFException{
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Foodstall is not found");
		}
		
		List<Offer> offers = offerRepository.getOffers(fsId);
		
		return offers;
	}
	
	public OfferResponse getOffer(Long fsId, Long offerId) throws TFException{
		
		OfferResponse offerResponse = new OfferResponse();
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Foodstall is not found");
		}
		
		Offer offer = offerRepository.getOffer(fsId, offerId);
		
		List<FoodItemsList> itemsLists = offerRepository.getOfferFoodItemLists(offerId);
		
		Map<String, List<FoodItemsList>> listsMap = new TreeMap<String, List<FoodItemsList>>();
		
		for(FoodItemsList list : itemsLists) {
			if(!listsMap.containsKey(list.getListName())) {
				listsMap.put(list.getListName(), new ArrayList<FoodItemsList>());
			}
			
			listsMap.get(list.getListName()).add(list);
		}
		
		offerResponse.setCategory(offer.getCategory());
		offerResponse.setCuisine(offer.getCuisine());
		offerResponse.setFsId(offer.getFsId());
		offerResponse.setItemsLists(listsMap);
		offerResponse.setOfferDate(offer.getOfferDate());
		offerResponse.setOfferId(offer.getOfferId());
		offerResponse.setOfferImage(offer.getOfferImage());
		offerResponse.setOfferPrice(offer.getOfferPrice());
		offerResponse.setOfferType(offer.getOfferType());
		offerResponse.setSubCategory(offer.getSubCategory());
		offerResponse.setTitle(offer.getTitle());
		offerResponse.setTotalPrice(offer.getTotalPrice());
		
		return offerResponse;
	}
	
	public void saveOfferFoodItemsLists(Long fsId, Long offerId, List<OfferFoodItemsListRequest> foodItemsLists) throws TFException{
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Foodstall is not found");
		}

		int i = 1;
		for(OfferFoodItemsListRequest singleList : foodItemsLists) {
			
			for(Item item : singleList.getFoodItems()) {
			
				FoodItemsList listItem = new FoodItemsList();
				
				listItem.setActualPrice(item.getActualPrice());
				listItem.setCustomizationFlag(item.isCustomizationFlag());
				listItem.setDescription(singleList.getDescription());
				listItem.setFoodItemId(item.getItemId());
				listItem.setFsId(fsId);
				listItem.setItemName(item.getItemName());
				listItem.setListName("LIST-" + i);
				listItem.setOfferId(offerId);
				listItem.setOfferPrice(item.getOfferPrice());
			
				offerRepository.createOfferFoodItemsList(listItem);
			}
			i++;
		}
		log.info("Food items lists are saved for offer {}", offerId);
	}
	
	public void saveOfferSuggestionFoodItems(Long fsId, Long offerId, OfferSuggestionItemRequest offerSuggestionItemsRequest) throws TFException{
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Foodstall is not found");
		}
		
		for(SuggestItem item : offerSuggestionItemsRequest.getSuggestionItems()) {
			
			SuggestionItem suggestionItem = new SuggestionItem();
			suggestionItem.setButtonType(offerSuggestionItemsRequest.getButtonType());
			suggestionItem.setCustomerSpecification(offerSuggestionItemsRequest.getCustomerSpecification());
			suggestionItem.setDescription(offerSuggestionItemsRequest.getDescription());
			suggestionItem.setFsId(fsId);
			suggestionItem.setItemId(item.getItemId());
			suggestionItem.setOfferId(offerId);
			suggestionItem.setSuggestionItemName(item.getItemName());
			
			
			offerRepository.createOfferSuggestionFoodItem(suggestionItem);
		}
		log.info("Food items lists are saved for offer {}", offerId);
	}
	
	private Long getIdForNewOffer() {

		Long offerId = commonSequenceService
				.getNextSequence(MongoCollectionConstant.COLLECTION_OFFER_SEQ);

		return offerId;
	}
}