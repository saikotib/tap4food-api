package com.endeavour.tap4food.app.model.fooditem;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = MongoCollectionConstant.COLLECTION_FOODITEM_PICS)
public class FoodItemPic {

	@Id
	private String id;
	
	private Long foodItemId;
	
	private Binary itemPic;
}
