package com.endeavour.tap4food.app.model.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_ORDERED_OFFER_ITEMS)
public class OrderedOfferItems {
	
	@Id
	private String id;

	private Long foodItemId;

	private String foodItemName;
	
	private String customizationName;

	private String customizationItem;

	private Double price;

	private Long cartItemId;
	
	private long quantity;
	
	private Double actualPrice;
	
	private Double offerPrice;
}
