package com.endeavour.tap4food.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class MerchantBankDetails {

	private String merchantId;
	
	private String bankName;
	
	private String ifscCode;
	
	private String branchName;
	
	private String state;
	
	private String city;
	
	private String country;
}
