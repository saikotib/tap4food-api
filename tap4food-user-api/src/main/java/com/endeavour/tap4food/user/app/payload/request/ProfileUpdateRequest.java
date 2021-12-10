package com.endeavour.tap4food.user.app.payload.request;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

	private String email;
	
	private String fullName;
}
