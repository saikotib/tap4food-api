package com.endeavour.tap4food.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.MerchantBankDetails;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.repository.MerchantRepository;
import com.endeavour.tap4food.app.util.AppConstants;
import com.endeavour.tap4food.app.util.AvatarImage;
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

	@Autowired
	PasswordEncoder encoder;

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
		
		Optional<Merchant> draftedMerchantData = this.findByPhoneNumber(merchant.getPhoneNumber());
		
		System.out.println(">>" + draftedMerchantData);
		
		if(draftedMerchantData.isPresent()) {
			Merchant draftedMerchant = draftedMerchantData.get();
			merchant.setUniqueNumber(draftedMerchant.getUniqueNumber());
			merchant.setId(draftedMerchant.getId());
		}else {
			Long uniqNumber = this.getUniqueNumber();

			Long currentTimeInMilli = System.currentTimeMillis();

			merchant.setUniqueNumber(uniqNumber);
			merchant.setCreatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));
			merchant.setLastUpdatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));

		}
		
		boolean isUserSaved = merchantRepository.save(merchant);
		
		if (isUserSaved) {
			commonService.sendOTPToPhone(merchant.getPhoneNumber());
		}
	}

	public boolean resentOtp(final String phoneNumber) {
		boolean flag = false;

		Optional<Merchant> merchantData = merchantRepository.findByPhoneNumber(phoneNumber);

		if (merchantData.isPresent()) {

			if (commonService.sendOTPToPhone(phoneNumber)) {
				flag = true;
			}
		}

		return flag;
	}

	public String validateSignupData(final String emailId, final String phoneNumber) {

		String validationMessage = null;

		Optional<Merchant> userByEmail = this.findByEmailId(emailId);

		if (userByEmail.isPresent()) {
			
			if(userByEmail.get().isPhoneNumberVerified()) {
				validationMessage = "The email id is already used..";

				return validationMessage;
			}
			
		}

		Optional<Merchant> userByPhOptional = this.findByPhoneNumber(phoneNumber);

		if (userByPhOptional.isPresent()) {
			if(userByEmail.get().isPhoneNumberVerified()) {
				validationMessage = "The phone number is already used.";

				return validationMessage;
			}
			
		}

		return null;

	}

	public boolean verifyOTP(final String phoneNumber, final String inputOTP, final boolean forgotPasswordFlag) {

		boolean otpMatch = false;

		Otp otp = commonRepository.getRecentOtp(phoneNumber);

		if (inputOTP.equalsIgnoreCase(otp.getOtp())) {
			otpMatch = true;
		} else {
			return otpMatch;
		}

		Optional<Merchant> merchantOptionalObject = merchantRepository.findByPhoneNumber(phoneNumber);

		if (merchantOptionalObject.isPresent()) {
			Merchant merchant = merchantOptionalObject.get();

			String merchantEmail = merchant.getEmail();

			String createPasswordLink = null;

			String message = null;

			String subject = null;

			if (forgotPasswordFlag) {

				subject = "Tap4Food merchant password reset";

				createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/merchant/createPassword?uniqueNumber="
						+ merchant.getUniqueNumber();

				message = commonService.getResetPasswordHtmlContent()
						.replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(merchant.getUniqueNumber()));

			} else {
				createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/merchant/createPassword?uniqueNumber="
						+ merchant.getUniqueNumber();

				message = commonService.getCreatePasswordHtmlContent()
						.replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(merchant.getUniqueNumber()));

				subject = "Tap4Food registration successfull";
			}

			sendMail(merchantEmail, message, subject);
			
			merchant.setPhoneNumberVerified(true);
			merchantRepository.phoneVerifyStatusUpdate(merchant);
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

		Optional<Merchant> merchantOptionalObject = merchantRepository
				.findByMerchantByPhoneNumber(merchant.getPhoneNumber());

		if (merchantOptionalObject.isPresent()) {

			merchant = merchantOptionalObject.get();

			if (Objects.isNull(merchant.getUniqueNumber())) {

				Long uniqNumber = this.getUniqueNumber();

				merchant.setUniqueNumber(uniqNumber);

				merchantRepository.updateUniqueNumber(merchant);

				System.out.println("Unique number is updated forthe merchant...");

				String merchantEmail = merchant.getEmail();

				String createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/merchant/createPassword?uniqueNumber="
						+ uniqNumber;

				String message = commonService.getCreatePasswordHtmlContent()
						.replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
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

				// Above code is to run the mail sending logic in background. END
			}
		}

		return flag;
	}

	public Merchant merchantStatusUpdate(Long uniqueNumber, String status) {

		Merchant merchant = null;

		Optional<Merchant> merchantResponse = merchantRepository.findByUniqueNumber(uniqueNumber);
		System.out.println("merchant response" + merchantResponse.get());
		if (merchantResponse.isPresent()) {

			merchant = merchantResponse.get();

			merchant.setStatus(status);

			merchant = merchantRepository.updateMerchant(merchant, false);
		}

		// Mail has to go here.

		return merchant;
	}

	public Merchant updateMerchant(@Valid Merchant merchant) {

		return merchantRepository.updateMerchant(merchant, false);
	}

	

	public Optional<Merchant> uploadProfilePic(final Long id, MultipartFile image, String imagetype) {

		Merchant merchantObj = new Merchant();
		Optional<Merchant> merchant = merchantRepository.findMerchantByUniqueId(id);

		if (merchant.isPresent()
				&& (imagetype.equals(AppConstants.PROFILE_PIC) || imagetype.equals(AppConstants.PERSONAL_ID))) {
			merchantObj = merchant.get();

			try {
				if (imagetype.equals(AppConstants.PROFILE_PIC)) {
					System.out.println("servoce if");
					merchantObj.setProfilePic(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
				} else if (imagetype.equals(AppConstants.PERSONAL_ID)) {
					merchantObj.setPersonalIdCard(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			merchantRepository.save(merchantObj);
		}else {
			merchant = null;
		}

		return merchant;
	}

	public String changePassword(final Long uniqueNumber, final String oldPassword, final String newPassword) throws TFException {

		String message = null;

		Optional<Merchant> merchantData = merchantRepository.findByUniqueNumber(uniqueNumber);

		if (merchantData.isPresent()) {
			Merchant merchant = merchantData.get();

			System.out.println("Is password matched :" + encoder.matches(oldPassword, merchant.getPassword()));

			if (encoder.matches(oldPassword, merchant.getPassword())) {

				merchant.setPassword(encoder.encode(newPassword));

				merchant = merchantRepository.updateMerchant(merchant, true);

				if(Objects.isNull(merchant)) {
					message = "Password is changed successfully";
				}else {
					message = "Merchant data couldn't found";
				}
					
			} else {
				message = "Old password is incorrect";
			}
		}else {
			throw new TFException("Invalid merchant ID");
		}

		return message;
	}

	public Optional<Merchant> saveMerchantBankDetails(@Valid Long uniqueId, MerchantBankDetails merchantBankDetails) {

		Optional<Merchant> merchantData = merchantRepository.findByUniqueNumber(uniqueId);
		Optional<MerchantBankDetails> merchantBankDetailsRes = merchantRepository
				.findMerchantBankDetailsByUniqueNumber(uniqueId);
		
		

		
		
		if(!merchantBankDetailsRes.isPresent()){
			if (merchantData.isPresent() && !merchantBankDetailsRes.isPresent()) {
				System.out.println("if");
				Merchant merchant = merchantData.get();
				merchantBankDetails.setMerchantId(uniqueId);
				merchantBankDetails = merchantRepository.saveMerchantBankDetails(merchantBankDetails);
				merchant.setBankDetails(merchantBankDetails);
				merchantRepository.save(merchant);
			}else {
				merchantData = null;
			}
		}else {
			if (merchantData.isPresent() && merchantBankDetailsRes.isPresent()) {
				
				merchantBankDetails.setId(merchantBankDetailsRes.get().getId());
				merchantRepository.saveMerchantBankDetails(merchantBankDetails);
				
				merchantData.get().setBankDetails(merchantBankDetails);
				
				merchantRepository.save(merchantData.get());
			}
			
		}
		
		

		return Optional.ofNullable(merchantData.get());
	}

	public Optional<MerchantBankDetails> getBankDetailsByUniqueId(final Long uniqueId) {

		return merchantRepository.findMerchantBankDetailsByUniqueNumber(uniqueId);
	}

	public Optional<Merchant> getMerchantDetailsByUniqueId(final Long uniqueNumber) {
		Optional<Merchant> merchantData = merchantRepository.findByUniqueNumber(uniqueNumber);
		return merchantData;
	}

	public Optional<Merchant> deleteProfilePic(@Valid Long id, String type) {

		Merchant merchantObj = new Merchant();
		Optional<Merchant> merchant = merchantRepository.findMerchantByUniqueId(id);

		if (merchant.isPresent()
				&& (type.equals(AppConstants.PROFILE_PIC) || type.equals(AppConstants.PERSONAL_ID))) {
			merchantObj = merchant.get();

			try {
				if (type.equals(AppConstants.PROFILE_PIC)) {

					merchantObj.setProfilePic(new Binary(BsonBinarySubType.BINARY,(new AvatarImage()).avatarImage()));
				} else if (type.equals(AppConstants.PERSONAL_ID)) {
					merchantObj.setPersonalIdCard(new Binary(BsonBinarySubType.BINARY,(new AvatarImage()).avatarImage()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			merchantRepository.save(merchantObj);
		}else {
			merchant = null;
		}

		return merchant;
	}

}
