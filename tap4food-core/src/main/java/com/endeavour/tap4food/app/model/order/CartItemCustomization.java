package com.endeavour.tap4food.app.model.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_CARTITEM_CUSTOMIZATION)
public class CartItemCustomization {
	
	@Id
	private String id;

	private Long foodItemId;

	private String customizationName;

	private String customizationItem;

	private Double price;

	private Long cartItemId;
}
