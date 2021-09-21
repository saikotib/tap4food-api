package com.endeavour.tap4food.app.model.offer;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_OFFERS_OFFER_SUGGESTION_ITEMS)
public class SuggestionItem {

	@Id
	private String id;
	
	private Long offerId;
	
	private String description;
	
	private Long itemId;
	
	private String suggestionItemName;
	
	private String customerSpecification;
	
	private String buttonType;
	
	private Long fsId; 
}
