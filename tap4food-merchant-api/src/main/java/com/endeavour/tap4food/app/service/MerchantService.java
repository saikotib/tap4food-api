package com.endeavour.tap4food.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.model.DatabaseSequence;
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

		Long uniqNumber = this.getUniqueNumber();

		Long currentTimeInMilli = System.currentTimeMillis();

		merchant.setUniqueNumber(uniqNumber);
		merchant.setCreatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));
		merchant.setLastUpdatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));

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

			merchantResponse = merchantRepository.saveMerchant(merchant);
		}
		;

		// Mail has to go here.

		return merchant;
	}

	public Optional<Merchant> updateMerchant(@Valid Merchant merchant) {

		return merchantRepository.saveMerchant(merchant);
	}

	public void addCategory(@Valid Category category) {

		merchantRepository.saveCategory(category);
	}

	
	public void addSubCategory(@Valid SubCategory subCategory) {
		merchantRepository.saveSubCategory(subCategory);
	}
	
	public void editCategory(@Valid Category category) {
		
//		List<Category> categoriesList = merchantRepository.findAllCategories();
//		String categoryName = category.getCategory();
//		if (categoriesList.contains(categoryName)) {
//			categoriesList.remove(categoryName);
//		}
		
		//category.setCategory(categoryName);
		merchantRepository.saveCategory(category);
	}
	
	public void editSubCategory(@Valid SubCategory subCategory) {
		merchantRepository.saveSubCategory(subCategory);
	}
	
	public void deleteCategory(@Valid Category category) {
		merchantRepository.deleteCategory(category);
	}
	
	public void deleteSubCategory(@Valid SubCategory subCategory) {
		merchantRepository.deleteSubCategory(subCategory);
	}
	
	public void hideCategory(@Valid Category category) {
		if (category.getVisible().equals(false)) {
			category.setVisible(false);
			merchantRepository.saveCategory(category);
		} else {
			category.setVisible(true);
			merchantRepository.saveCategory(category);
		}
	}
	
	public void hideSubCategory(@Valid SubCategory subCategory) {
		if (subCategory.getVisible().equals(false)) {
			subCategory.setVisible(false);
			merchantRepository.saveSubCategory(subCategory);
		} else {
			subCategory.setVisible(true);
			merchantRepository.saveSubCategory(subCategory);
		}
	}

	public List<Category> getAllCategories() {
		Optional<List<Category>> categoryId = merchantRepository.findAllCategories();
		if (categoryId.isPresent()) {

			return categoryId.get();
		} else {
			return new ArrayList<Category>();
		}
	}

	public List<SubCategory> getAllSubCategories() {
		Optional<List<SubCategory>> categoriesList = merchantRepository.findAllSubCategories();
		if (categoriesList.isPresent()) {

			return categoriesList.get();
			
		} else {
			return new ArrayList<SubCategory>();
		}
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

	public String changePassword(final Long uniqueNumber, final String oldPassword, final String newPassword) {

		String message = null;

		Optional<Merchant> merchantData = merchantRepository.findByUniqueNumber(uniqueNumber);

		if (merchantData.isPresent()) {
			Merchant merchant = merchantData.get();

			System.out.println("Is password matched :" + encoder.matches(oldPassword, merchant.getPassword()));

			if (encoder.matches(oldPassword, merchant.getPassword())) {

				merchant.setPassword(encoder.encode(newPassword));

				merchantRepository.saveMerchant(merchant);

				message = "Password is changed successfully";
			} else {
				message = "Old password is incorrect";
			}
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

	public Optional<FoodStallTimings> saveFoodCourtTimings(Long uniqueId, ArrayList<WeekDay> weekDay) {

		Optional<Merchant> merchantData = merchantRepository.findByUniqueNumber(uniqueId);
		FoodStallTimings foodStallTimings = new FoodStallTimings();
		if (merchantData.isPresent()) {	
			foodStallTimings.setMerchantId(uniqueId);
			foodStallTimings.setFoodStalltId(merchantRepository.getFoodCourtUniqueNumber());
			foodStallTimings = merchantRepository.savefoodStallTimings(foodStallTimings);
			for (int i = 0; i < weekDay.size(); i++) {
				weekDay.get(i).setFoodStallId(foodStallTimings.getFoodStalltId());
				merchantRepository.saveOneWeekDay(weekDay.get(i));
			}

			/* Collection<WeekDay> res = merchantRepository.saveWeekDay(); */

			foodStallTimings.setDays(weekDay);
		}
	

		return Optional.ofNullable(foodStallTimings);
	}

	public List<WeekDay> getFoodCourtTimingsByUniqueId(final String uniqueId) {

		return merchantRepository.findWeekDayByFoodCourtUniqueNumber(uniqueId);
	}

	public Optional<MerchantBankDetails> getBankDetailsByUniqueId(final Long uniqueId) {

		return merchantRepository.findMerchantBankDetailsByUniqueNumber(uniqueId);
	}

	public Collection<WeekDay> updateFoodCourtTimings(final String uniqueId, ArrayList<WeekDay> weekDay) {
		
		List<WeekDay> weekRes = merchantRepository.findWeekDayByFoodCourtUniqueNumber(uniqueId);
		//Optional<Merchant> merchant = merchantRepository.findByUniqueNumber(uniqueNumber)
		
		for (int i = 0; i < weekRes.size(); i++) {
			/* WeekDay weekDayObj = weekRes.get(i); */
			weekDay.get(i).setId(weekRes.get(i).getId());
			merchantRepository.saveOneWeekDay(weekDay.get(i));
		}
		
		

		return weekRes.stream().collect(Collectors.toSet());
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

	public void addCustomizeType(@Valid CustomizeType customizeType) {
		
		merchantRepository.saveCustomizeType(customizeType);
	}

	public void editCustomizeType(@Valid CustomizeType customizeType) {
		merchantRepository.saveCustomizeType(customizeType);
	}

	public void deleteCustomizeType(@Valid CustomizeType customizeType) {
		merchantRepository.removeCustomizeType(customizeType);
	}

	public void hideCustomizeType(@Valid CustomizeType customizeType) {
		if (customizeType.getVisible().equals(false)) {
			customizeType.setVisible(false);
			merchantRepository.saveCustomizeType(customizeType);
			
		} else {
			customizeType.setVisible(true);
			merchantRepository.saveCustomizeType(customizeType);
		}
		
	}

	public Cuisine addCuisineName(@Valid Cuisine cuisine) throws Exception {
		Cuisine saveCuisine = merchantRepository.saveCuisine(cuisine);
		return saveCuisine;
		
	}

	public void editCusine(@Valid Cuisine cuisine) throws Exception {
		merchantRepository.saveCuisine(cuisine);
		
	}

	public void deleteCustomizeType(@Valid Cuisine cuisine) {
		merchantRepository.removeCuisine(cuisine);
		
	}

	public void hideCustomizeType(@Valid Cuisine cuisine) throws Exception {
		
		if (cuisine.getVisible().equals(false)) {
			cuisine.setVisible(false);
			merchantRepository.saveCuisine(cuisine);
			
		}  else {
			cuisine.setVisible(true);
			merchantRepository.saveCuisine(cuisine);
		}
	}

}
