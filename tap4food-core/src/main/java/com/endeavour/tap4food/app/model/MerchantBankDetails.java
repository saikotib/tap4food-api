package com.endeavour.tap4food.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "merchantBankDetails")
public class MerchantBankDetails {

	@Id
	private String id;
	
	private Long merchantId;
	
	private String bankName;
	
	private String ifscCode;
	
	private String branchName;
	
	private String state;
	
	private String city;
	
	private String country;
}
