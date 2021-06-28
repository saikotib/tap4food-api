package com.endeavour.tap4food.app.model;

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
	
	private String businessUnitType;   // ShoppingMall/Theatre/Restaurant
	
	private String foodCourtName;
	
	private String foodStallName;
	
	private String businessUnitName;
	
	private String phoneNumber;
	
	private String email;
	
	private String personalIdNumber;  // This is the ID card number of the merchant contact person.

	private Binary personalIdCard;
	
	private String gstNumber;  //GST number
	
	private String taxIdentificationNumber;   // Tax Identification Number
	
	private String foodStallLicenseNumber;
	
	private String deliveryTime;  // This is food delivery time (ex: 20 mins)
	
	private String password;
	
	@DBRef
	private MerchantBankDetails bankDetails;
	
}
