package com.endeavour.tap4food.app.model.offer;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_OFFERS)
public class Offer {

	@Id
	private String id;
	
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
	
	@JsonIgnore
	private String requestId;
}
