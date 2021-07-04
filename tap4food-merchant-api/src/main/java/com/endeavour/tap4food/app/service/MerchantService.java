package com.endeavour.tap4food.app.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.MenuCategory;
import com.endeavour.tap4food.app.model.MenuSubCategory;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.repository.MerchantRepository;
import com.endeavour.tap4food.app.util.DateUtil;
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
			validationMessage = "The email id is already used..";

			return validationMessage;
		}

		Optional<Merchant> userByPhOptional = this.findByPhoneNumber(phoneNumber);

		if (userByPhOptional.isPresent()) {
			validationMessage = "The phone number is already used.";

			return validationMessage;
		}

		return null;

	}

	public boolean verifyOTP(final String phoneNumber, final String inputOTP, final boolean forgotPasswordFlag) {

		boolean otpMatch = false;

		Otp otp = commonRepository.getRecentOtp(phoneNumber);

		if (inputOTP.equalsIgnoreCase(otp.getOtp())) {
			otpMatch = true;
		}else {
			return otpMatch;
		}

		Optional<Merchant> merchantOptionalObject = merchantRepository.findByPhoneNumber(phoneNumber);
		
		Merchant merchant = merchantOptionalObject.get();
		
		if(forgotPasswordFlag) {
			
			String merchantEmail = merchant.getEmail();
			
			String createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/merchant/createPassword?uniqueNumber=" + merchant.getUniqueNumber();
			
			String message = commonService.getResetPasswordHtmlContent().replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
					.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(merchant.getUniqueNumber()));
			
			String subject = "Tap4Food password reset";
			
			sendMail(merchantEmail, message, subject);
			
		}else if(merchantOptionalObject.isPresent()) {
			
			if(Objects.isNull(merchant.getUniqueNumber())) {
				
				Long uniqNumber = this.getUniqueNumber();
				
				merchant.setUniqueNumber(uniqNumber);
				
				Long currentTimeInMilli = System.currentTimeMillis();
				
				merchant.setCreatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));
				merchant.setLastUpdatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));
				
				merchantRepository.updateUniqueNumber(merchant);
				
				System.out.println("Unique number is updated forthe merchant...");
				
				String merchantEmail = merchant.getEmail();
				
				String createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/merchant/createPassword?uniqueNumber=" + uniqNumber;
				
				String message = commonService.getCreatePasswordHtmlContent().replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(uniqNumber));
				
				String subject = "Tap4Food registration successfull";
				
				sendMail(merchantEmail, message, subject);
				
			}
		}
		
		return otpMatch;
	}
	
	private void sendMail(String merchantEmail, String message, String subject) {
		ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
        emailExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	commonService.sendEmail(merchantEmail, message, subject);
            }
        });
        emailExecutor.shutdown();
	}

	private Long getUniqueNumber() {

		Long uniqNumber = merchantRepository.getRecentUniqueNumber();

		return uniqNumber;
	}
	
	public boolean createPassword(final Long uniqueNumber, final String password) {
		
		merchantRepository.createPassword(uniqueNumber, password);
		
		return true;
	}

	public boolean createMerchant(Merchant merchant) {
		boolean flag = false;
		flag = merchantRepository.createMerchant(merchant);
		
		Optional<Merchant> merchantOptionalObject = merchantRepository.findByMerchantByPhoneNumber(merchant.getPhoneNumber());
		
		if(merchantOptionalObject.isPresent()) {
			
			merchant = merchantOptionalObject.get();
			
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
				
				// Below code is to run the mail sending logic in background. START
				
				ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
		        emailExecutor.execute(new Runnable() {
		            @Override
		            public void run() {
		            	commonService.sendEmail(merchantEmail, message, subject);
		            }
		        });
		        emailExecutor.shutdown();				
				
		        //Above code is to run the mail sending logic in background. END
			}
		}

		return flag;
	}
	
	
	public Merchant merchantStatusUpdate(Long uniqueNumber, String status) {
		
		Merchant merchant = null;
		
		Optional<Merchant> merchantResponse = merchantRepository.findByUniqueNumber(uniqueNumber);
		System.out.println("merchant response" + merchantResponse.get());
		if(merchantResponse.isPresent()) {
			
			merchant = merchantResponse.get();
			
			merchant.setStatus(status);
			
			merchantResponse = merchantRepository.saveMerchant(merchant);
		};
		
		//Mail has to go here.
		

		return merchant;
	}

	public Optional<Merchant> updateMerchant(@Valid Merchant merchant) {

		return merchantRepository.saveMerchant(merchant);
	}


	public void createMenuCategory(@Valid MenuCategory menuCategory) {
		
		 merchantRepository.addMenuCategory(menuCategory); 
	}

	public void createMenuSubCategory(@Valid MenuSubCategory menuSubCategory) {
		
		merchantRepository.addMenuSubCategory(menuSubCategory);
	}
	
	public List<MenuCategory> getAllCategories() {
		Optional<List<MenuCategory>> categoryId = merchantRepository.findAllCategories();
		if (categoryId.isPresent()) {
			
			return categoryId.get();
		} else {
			return new ArrayList<MenuCategory>();
		}
	}

	public Set<MenuSubCategory> getAllSubCategories(String id) {
		Optional<MenuCategory> categoryId = merchantRepository.findAllSubCategories(id);
		if (categoryId.isPresent()) {
			
			return categoryId.get().getSubCategories();
		} else {
			return new HashSet<MenuSubCategory>();
		}
	}

}
