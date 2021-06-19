package com.endeavour.tap4food.app.model;

import org.bson.types.Binary;

import lombok.Data;

@Data
public class Merchant {

	private String uniqId;
	
	private String userName;
	
	private Binary profilePic;
	
	private String country;
	
	private String state;
	
	private String city;
	
	private String businessUnitType;   // ShoppingMall/Theatre/Restaurant
	
	private String foodStallName;
	
	private String businessUnitName;
	
	private String phoneNumber;
	
	private String email;
	
	private String personalIdNumber;  // This is the ID card number of the merchant contact person.

	private Binary personalIdCard;
	
	private String gstNumber;  //GST number
	
	private String taxIdentificationNumber;   // Tax Identification Number
	
	private String foodStallLicenseNumber;
	
	private String deliveryTime;  // This is food delivery time
	
	
}
