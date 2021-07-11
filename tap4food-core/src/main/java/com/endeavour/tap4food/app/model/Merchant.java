package com.endeavour.tap4food.app.model;

import java.util.List;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_MERCHANT)
public class Merchant {
	
	@Id
	private String id;

	private Long uniqueNumber;
	
	private String userName;
	
	private Binary profilePic;
	
	private String country;
	
	private String state;
	
	private String city;
	
	private String phoneNumber;
	
	private String email;
	
	private String personalIdNumber;  // This is the ID card number of the merchant contact person.

	private Binary personalIdCard;
	
	private String password;
	
	private String createdBy;
	
	private String status;
	
	private String createdDate;
	
	private String lastUpdatedDate;
	
	@DBRef
	private MerchantBankDetails bankDetails;
	
	@DBRef
	private List<FoodStall> foodStalls;
	
}
