package com.endeavour.tap4food.app.model;

import java.util.List;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "foodStalls")
public class FoodStall {

	@Id
	private String id;

	private String foodStallName;

	private Long foodStallId;

	private String gstNumber; // GST number

	private String taxIdentificationNumber; // Tax Identification Number

	private String foodStallLicenseNumber;

	private String deliveryTime; // This is food delivery time (ex: 20 mins)

	private Long merchantUniqueNumber;

	private Long foodCourtId;
	
	private String country;
	
	private String state;
	
	private String city;
	
	private String location;
	
	private String foodCourtName;
	
	private String buType;
	
	private String buName;
	
	private Long buId;
	
	private List<Binary> foodStallPics;
	
	private List<Binary> menuPics;
	
	private Double rating;
	
	@DBRef
	private MenuListings menuListing;

	@DBRef
	private FoodStallTimings foodStallTimings;
}
