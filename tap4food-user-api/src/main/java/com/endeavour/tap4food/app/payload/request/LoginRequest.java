package com.endeavour.tap4food.app.payload.request;

import javax.validation.constraints.NotBlank;

public class LoginRequest {

	@NotBlank
	private String phoneNumber;

	@NotBlank
	private String otp;
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

}
