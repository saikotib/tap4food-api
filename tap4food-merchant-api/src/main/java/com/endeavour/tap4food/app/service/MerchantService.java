package com.endeavour.tap4food.app.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.repository.MerchantRepository;

@Service
public class MerchantService {

	@Autowired
	private MerchantRepository merchantRepository;
	
	@Autowired
	private CommonRepository commonRepository;

	@Autowired
	private CommonService commonService;

	public Optional<Merchant> findByEmailId(String emailId) {

		return merchantRepository.findByEmailId(emailId);
	}

	public Optional<Merchant> findByPhoneNumber(String phoneNumber) {

		return merchantRepository.findByPhoneNumber(phoneNumber);
	}
	
	public Optional<Merchant> findByUniqueNumber(Long uniqueNumber) {

		return merchantRepository.findByUniqueNumber(uniqueNumber);
	}

	public void saveUser(Merchant merchant) {

		boolean isUserSaved = merchantRepository.save(merchant);

		if (isUserSaved) {
			commonService.sendOTPToPhone(merchant.getPhoneNumber());
		}
	}

	public String validateSignupData(final String emailId, final String phoneNumber) {

		String validationMessage = null;

		Optional<Merchant> userByEmail = this.findByEmailId(emailId);

		if (userByEmail.isPresent()) {
			validationMessage = "The email id is already used.";

			return validationMessage;
		}

		Optional<Merchant> userByPhOptional = this.findByPhoneNumber(phoneNumber);

		if (userByPhOptional.isPresent()) {
			validationMessage = "The phone number is already used.";

			return validationMessage;
		}

		return null;

	}

	public boolean verifyOTP(final String phoneNumber, final String inputOTP) {

		boolean otpMatch = false;

		Otp otp = commonRepository.getRecentOtp(phoneNumber);

		if (inputOTP.equalsIgnoreCase(otp.getOtp())) {
			otpMatch = true;
		}

		Long uniqNumber = this.getUniqueNumber();
		
		Optional<Merchant> merchantOptionalObject = merchantRepository.findByPhoneNumber(phoneNumber);
		
		if(merchantOptionalObject.isPresent()) {
			
			Merchant merchant = merchantOptionalObject.get();
			
			merchant.setUniqueNumber(uniqNumber);
			
			merchantRepository.updateUniqueNumber(merchant);
			
			System.out.println("Unique number is updated forthe merchant...");
		}
		/*
		String message = "http://localhost:3000/createPassword?uniqueNumber=" + uniqNumber;
		
		commonService.sendSMS(phoneNumber, message);
		
		*/

		System.out.println("Unique Number : " + uniqNumber);

		return otpMatch;
	}

	private Long getUniqueNumber() {

		Long uniqNumber = merchantRepository.getRecentUniqueNumber();

		return uniqNumber;
	}
	
	public boolean createPassword(final Long uniqueNumber, final String password) {
		
		merchantRepository.createPassword(uniqueNumber, password);
		
		
		return true;
	}

}
