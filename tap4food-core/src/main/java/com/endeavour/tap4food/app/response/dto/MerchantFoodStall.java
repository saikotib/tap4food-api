package com.endeavour.tap4food.app.response.dto;

import java.util.Set;

import lombok.Data;

@Data
public class MerchantFoodStall {

	private Long merchantId;
	
	private String foodStallName;
	
	private Long foodStallId;
	
	private String owner;
	
	private String phoneNumber;
	
	private String email;
	
	private String userName;
	
	private String subscriptionDetails;
	
	private String date;
	
	private String status;
	
	private String address;
	
	private String country;
	
	private String state;
	
	private String city;
	
	private String location;
	
	private String buType;
	
	private String buName;
	
	private Long buId;
	
	private String fcName;
	
	private String fcQRCode;
	
	private String deliveryTime;
	
	private String gstNumber;
	
	private String licenceNumber;
	
	private Set<String> menuPics;
	
	private Set<String> stallPics;
	
	private String personalIDNumber;
	
	private String personalIDUrl;
	
	private String profilePic;
}
