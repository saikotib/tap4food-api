package com.endeavour.tap4food.app.model;

import java.util.List;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.Data;

@Data
@Document(collection = MongoCollectionConstant.COLLECTION_MERCHANT)
public class Merchant {
	
	@Id
	private String id;

	@Indexed
	private Long uniqueNumber;
	
	private String userName;
	
	private String profilePic;
	
	private String phoneNumber;
	
	private String email;
	
	private String personalIdNumber;  // This is the ID card number of the merchant contact person.

	private String personalIdCard;
	
	private String password;
	
	private String createdBy;
	
	private String status;
	
	private String createdDate;
	
	private String lastUpdatedDate;
	
	private boolean isPhoneNumberVerified;
	
	private Long blockedTimeMs;
	
	private boolean isManager;
	
	private Long parentMerchant;
	
	@DBRef
	private MerchantBankDetails bankDetails;
	
	@DBRef
	private List<FoodStall> foodStalls;
	
}
