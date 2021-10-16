package com.endeavour.tap4food.app.model.order;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_CARTITEM)
public class CartItem {
	
	@Id
	private Long cartItemId;
	
	private Long foodItemId;
	
	private String itemName;
	
	private Double finalPrice;
	
	private Integer quantity;
	
	private boolean isPizza;
	
	private String appliedOfferName;
	
	private Long appliedOfferId;
	
	private boolean customizationFlag;
	
	private Long orderId;
	
}