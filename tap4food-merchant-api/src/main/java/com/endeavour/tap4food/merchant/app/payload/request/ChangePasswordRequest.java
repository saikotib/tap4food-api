package com.endeavour.tap4food.merchant.app.payload.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {

	private Long uniqueNumber;
	
	private String oldPassword;
	
	private String newPassword;
}
