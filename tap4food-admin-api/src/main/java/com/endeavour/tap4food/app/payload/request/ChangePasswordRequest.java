package com.endeavour.tap4food.app.payload.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {

	private String email;
	
	private String phoneNumber;
	
	private String oldPassword;
	
	private String newPassword;
}
