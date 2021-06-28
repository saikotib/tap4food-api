package com.endeavour.tap4food.app.payload.request;

import javax.validation.constraints.NotBlank;

public class LoginRequest {

	private Long uniqueNumber;

	@NotBlank
	private String password;
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getUniqueNumber() {
		return uniqueNumber;
	}

	public void setUniqueNumber(Long uniqueNumber) {
		this.uniqueNumber = uniqueNumber;
	}
	
}
