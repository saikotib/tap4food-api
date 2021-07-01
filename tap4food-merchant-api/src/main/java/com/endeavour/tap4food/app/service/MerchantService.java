package com.endeavour.tap4food.app.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.repository.MerchantRepository;
import com.endeavour.tap4food.app.util.EmailTemplateConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j	
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
		}else {
			return otpMatch;
		}

		Optional<Merchant> merchantOptionalObject = merchantRepository.findByPhoneNumber(phoneNumber);
		
		if(merchantOptionalObject.isPresent()) {
			
			Merchant merchant = merchantOptionalObject.get();
			
			if(Objects.isNull(merchant.getUniqueNumber())) {
				
				Long uniqNumber = this.getUniqueNumber();
				
				merchant.setUniqueNumber(uniqNumber);
				
				merchantRepository.updateUniqueNumber(merchant);
				
				System.out.println("Unique number is updated forthe merchant...");
				
				String merchantEmail = merchant.getEmail();
				
				String createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/merchant/createPassword?uniqueNumber=" + uniqNumber;
				
				String message = commonService.getCreatePasswordHtmlContent().replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(uniqNumber));
				
				String subject = "Tap4Food registration successfull";
				
				commonService.sendEmail(merchantEmail, message, subject);
			}
		}
		
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
