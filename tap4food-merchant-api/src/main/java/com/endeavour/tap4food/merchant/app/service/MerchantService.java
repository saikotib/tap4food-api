package com.endeavour.tap4food.merchant.app.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.enums.AccountStatusEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.MerchantBankDetails;
import com.endeavour.tap4food.app.model.MerchantContactAdmin;
import com.endeavour.tap4food.app.model.MerchantSettings;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.response.dto.StallManager;
import com.endeavour.tap4food.app.service.CommonService;
import com.endeavour.tap4food.app.util.ApiURL;
import com.endeavour.tap4food.app.util.AppConstants;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.app.util.EmailTemplateConstants;
import com.endeavour.tap4food.app.util.MediaConstants;
import com.endeavour.tap4food.merchant.app.repository.FoodStallRepository;
import com.endeavour.tap4food.merchant.app.repository.MerchantRepository;

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
	
	@Autowired
	private FoodStallRepository foodStallRepository;
	
	@Value("${images.server}")
	private String mediaServerUrl;
	
	private static final int OTP_EXPIRY_TIME_IN_MS = 5 * 60 * 1000;

	public Optional<Merchant> findByEmailId(String emailId) {

		return merchantRepository.findByEmailId(emailId);
	}

	public Optional<Merchant> findByPhoneNumber(String phoneNumber) {

		return merchantRepository.findByPhoneNumber(phoneNumber);
	}

	public Optional<Merchant> findByUniqueNumber(Long uniqueNumber) {

		return merchantRepository.findByUniqueNumber(uniqueNumber);
	}

	public void saveMerchant(Merchant merchant) {
		
		Optional<Merchant> draftedMerchantData = this.findByPhoneNumber(merchant.getPhoneNumber());
		
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
				validationMessage = "The email id is already used.";

				return validationMessage;
			}
			
		}

		Optional<Merchant> userByPhOptional = this.findByPhoneNumber(phoneNumber);

		if (userByPhOptional.isPresent()) {
			if(userByPhOptional.get().isPhoneNumberVerified()) {
				validationMessage = "The phone number is already used.";

				return validationMessage;
			}
			
		}

		return null;

	}

	public boolean verifyOTP(final String phoneNumber, final String inputOTP, final boolean forgotPasswordFlag) throws TFException {

		boolean otpMatch = false;

		Otp otp = commonRepository.getRecentOtp(phoneNumber);

		Long timeDiff = commonService.getTimeDiff(Long.valueOf(otp.getOtpSentTimeInMs()));
		
//		if(timeDiff > OTP_EXPIRY_TIME_IN_MS) {
//			throw new TFException("OTP is expired");
//		}
		
		if (inputOTP.equalsIgnoreCase(otp.getOtp())) {
			otpMatch = true;
			otp.setNumberOfTries(0);
		} else {
			if(otp.getNumberOfTries() == null) {
				otp.setNumberOfTries(1);
			}else if(otp.getNumberOfTries() >= 1 && otp.getNumberOfTries() < 3) {
				otp.setNumberOfTries(otp.getNumberOfTries() + 1);
			}else if(otp.getNumberOfTries() == 3) {
				otp.setNumberOfTries(otp.getNumberOfTries() + 1);
			}
			
//			commonRepository.saveOtp(otp);
//			
//			if(otp.getNumberOfTries() >= 3) {
//				throw new TFException("This phone number is temporarily blocked.");
//			}
			
			otpMatch = false;
		}

		Optional<Merchant> merchantOptionalObject = merchantRepository.findByPhoneNumber(phoneNumber);

		Merchant merchant = null;
		
		if (merchantOptionalObject.isPresent() && !otpMatch) {
			merchant = merchantOptionalObject.get();
//			merchant.setStatus(AccountStatusEnum.LOCKED.name());
			merchant.setBlockedTimeMs(System.currentTimeMillis());
			
			merchant.setPhoneNumberVerified(true);
			
			merchantRepository.updateMerchant(merchant, false);
		}
		
		if (merchantOptionalObject.isPresent() && otpMatch) {
			merchant = merchantOptionalObject.get();
			
			if(merchant.getStatus().equals(AccountStatusEnum.LOCKED.name())) {
				throw new TFException("Phone number is temporarily blocked");
			}

			String merchantEmail = merchant.getEmail();

			String createPasswordLink = null;

			String message = null;

			String subject = null;

			if (forgotPasswordFlag) {

				subject = "Tap4Food merchant password reset";

				createPasswordLink = ApiURL.API_URL + "/merchant/createPassword?uniqueNumber="
						+ merchant.getUniqueNumber();

				message = commonService.getResetPasswordHtmlContent()
						.replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(merchant.getUniqueNumber()));

			} else {
				createPasswordLink = ApiURL.API_URL + "/merchant/createPassword?uniqueNumber="
						+ merchant.getUniqueNumber();

				message = commonService.getCreatePasswordHtmlContent()
						.replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(merchant.getUniqueNumber()));

				subject = "Tap4Food registration successfull";
			}

			sendMail(merchantEmail, message, subject);
			
			merchant.setPhoneNumberVerified(true);
			merchantRepository.phoneVerifyStatusUpdate(merchant);
			
			commonService.createMediaFolderStructure(merchant.getUniqueNumber());
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
	
	public Merchant createStallManager(Merchant merchant, Long foodStallId, Long parentMerchantId) throws TFException {
		
		merchant.setManager(true);
		merchant.setParentMerchant(parentMerchantId);
		merchant.setStatus(AccountStatusEnum.ACTIVE.name());
		merchant.setPhoneNumberVerified(true);
		
		merchant = this.createMerchant(merchant);
		
		FoodStall foodStall = foodStallRepository.getFoodStallById(foodStallId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Foodstall not found.");
		}
		
		if(Objects.nonNull(foodStall.getManagerId())) {
			throw new TFException("Manager is already assigned for this foodstall.");
		}
				
		foodStall.setManagerId(merchant.getUniqueNumber());
		
		foodStallRepository.updateFoodStall(foodStall);
		
		return merchant;
	}

	public Merchant createMerchant(Merchant merchant) {
		
		merchant.setStatus("Active");
		
		merchantRepository.createMerchant(merchant);

		Optional<Merchant> merchantOptionalObject = merchantRepository
				.findByMerchantByPhoneNumber(merchant.getPhoneNumber());

		if (merchantOptionalObject.isPresent()) {

			merchant = merchantOptionalObject.get();

			if (Objects.isNull(merchant.getUniqueNumber())) {

				Long uniqNumber = this.getUniqueNumber();

				merchant.setUniqueNumber(uniqNumber);

				merchantRepository.updateUniqueNumber(merchant);

				System.out.println("Unique number is updated for the merchant...");

				String merchantEmail = merchant.getEmail();
				
				String createPasswordLink = ApiURL.API_URL + "/merchant/createPassword?uniqueNumber="
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

		return merchant;
	}

	public Merchant updateMerchant(Merchant merchant) {

		return merchantRepository.updateMerchant(merchant, false);
	}
	
	public Merchant updateMerchantStatus(Long merchantId, String status) throws TFException {

		Merchant merchant = merchantRepository.updateMerchantStatus(merchantId, status);
		
		//Mail has to go here. 
		
		return merchant;
	}

	public Optional<Merchant> uploadProfilePic(final Long id, MultipartFile image, String imagetype) {

		Merchant merchantObj = new Merchant();
		Optional<Merchant> merchant = merchantRepository.findMerchantByUniqueId(id);

		if (merchant.isPresent()
				&& (imagetype.equals(AppConstants.PROFILE_PIC) || imagetype.equals(AppConstants.PERSONAL_ID))) {
			merchantObj = merchant.get();

			try {
				if (imagetype.equals(AppConstants.PROFILE_PIC)) {
					
					String uploadPath = commonService.getMerhantMediaDirs().get(MediaConstants.GET_KEY_MERCHANT_PROFILE_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(id));
					
					new File(uploadPath).mkdirs();
					
					Path path = Paths.get(uploadPath);
					File existingFile = new File(uploadPath + File.separator + image.getOriginalFilename());
					
					if(existingFile.exists()) {
						if(existingFile.delete()) {
							log.info("Deleted the existing file");
						}
					}
					
					Files.copy(image.getInputStream(), path.resolve(image.getOriginalFilename()));
					
					log.info("Profile Image Path : " + uploadPath);
					log.info("Profile Image Name : " + image.getOriginalFilename());
					
					log.info("Is Base Loc found :" + uploadPath.contains(commonService.getMediaBaseLocation()));
					
					String profilePicLink = uploadPath.replaceAll(commonService.getMediaBaseLocation(), "").replaceAll("\\\\", "/");

					profilePicLink = mediaServerUrl + profilePicLink + "/" + image.getOriginalFilename();
					
					log.info("profilePicLink :" + profilePicLink);
					
					merchantObj.setProfilePic(profilePicLink);
				} else if (imagetype.equals(AppConstants.PERSONAL_ID)) {
					
					String uploadPath = commonService.getMerhantMediaDirs().get(MediaConstants.GET_KEY_MERCHANT_PERSONAL_ID_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(id));
					
					new File(uploadPath).mkdirs();
					
					Path path = Paths.get(uploadPath);
					File existingFile = new File(uploadPath + File.separator + image.getOriginalFilename());
					
					if(existingFile.exists()) {
						if(existingFile.delete()) {
							log.info("Deleted the existing file");
						}
					}
					
					Files.copy(image.getInputStream(), path.resolve(image.getOriginalFilename()));
					
					log.info("Personal ID Image Path : " + uploadPath);
					log.info("Personal ID Image Name : " + image.getOriginalFilename());
					
					log.info("Is Base Loc found :" + uploadPath.contains(commonService.getMediaBaseLocation()));
					
					String personalIdPicLink = uploadPath.replaceAll(commonService.getMediaBaseLocation(), "").replaceAll("\\\\", "/");

					personalIdPicLink = mediaServerUrl + personalIdPicLink + "/" + image.getOriginalFilename();
					
					log.info("Personal IDPicLink :" + personalIdPicLink);
					
					merchantObj.setPersonalIdCard(personalIdPicLink);
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

				if(Objects.nonNull(merchant)) {
					message = "Password is changed successfully";
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

	public Optional<Merchant> getMerchantDetailsByUniqueId(final Long uniqueNumber) throws TFException {
		Merchant merchant = merchantRepository.getMerchant(uniqueNumber);
		
		List<FoodStall> foodStallsList = new ArrayList<FoodStall>();
		
		if(merchant.isManager()) {
			List<FoodStall> foodStalls = foodStallRepository.getFoodStalls(uniqueNumber, true);
			for(FoodStall stall : foodStalls) {
				if(stall.getTax() == null) {
					stall.setTax(Double.valueOf(5));
				}
				FoodStallTimings timings = foodStallRepository.getFoodStallTimings(stall.getFoodStallId());
				
				stall.setFoodStallTimings(timings);
				
				if(Objects.isNull(stall.getQrCode())) {
					String qrCode = mediaServerUrl + "/QRCodes/" + stall.getFoodCourtId() + ".png";
					stall.setQrCode(qrCode);
				}
				
				if(stall.isRestaurant()) {
					foodStallsList.add(stall);
				}else {
					Optional<BusinessUnit> buData = foodStallRepository.findBusinessUnit(stall.getBuId());
					
					if(buData.isPresent()) {
						BusinessUnit bu = buData.get();
						
						stall.setBuName(bu.getName());
						
						foodStallsList.add(stall);
					}
				}
				
			}
			
		}else {
			List<FoodStall> foodStalls = foodStallRepository.getFoodStalls(uniqueNumber, false);
			
			for(FoodStall stall : foodStalls) {
				if(stall.getTax() == null) {
					stall.setTax(Double.valueOf(5));
				}
				FoodStallTimings timings = foodStallRepository.getFoodStallTimings(stall.getFoodStallId());
				
				stall.setFoodStallTimings(timings);
				
				if(Objects.isNull(stall.getQrCode())) {
					String qrCode = mediaServerUrl + "/QRCodes/" + stall.getFoodCourtId() + ".png";
					stall.setQrCode(qrCode);
				}
				
				if(stall.isRestaurant()) {
					foodStallsList.add(stall);
				}else {
					Optional<BusinessUnit> buData = foodStallRepository.findBusinessUnit(stall.getBuId());
					
					if(buData.isPresent()) {
						BusinessUnit bu = buData.get();
						
						stall.setBuName(bu.getName());
						
						foodStallsList.add(stall);
					}
				}			
				
			}
		}
		
		merchant.setFoodStalls(foodStallsList);

		return Optional.ofNullable(merchant);
	}	
	
	public List<StallManager> getStallManagers(final Long parentMerchantId) throws TFException {
		

		return merchantRepository.getStallManagers(parentMerchantId);
	}	

	public Optional<Merchant> deleteProfilePic(@Valid Long id, String type) {

		Merchant merchantObj = new Merchant();
		Optional<Merchant> merchant = merchantRepository.findMerchantByUniqueId(id);

		if (merchant.isPresent()
				&& (type.equals(AppConstants.PROFILE_PIC) || type.equals(AppConstants.PERSONAL_ID))) {
			merchantObj = merchant.get();

			if (type.equals(AppConstants.PROFILE_PIC)) {
				merchantObj.setProfilePic("");
			} else if (type.equals(AppConstants.PERSONAL_ID)) {
				merchantObj.setPersonalIdCard("");
			}

			merchantRepository.save(merchantObj);
		}else {
			merchant = null;
		}

		return merchant;
	}
	
	public List<BusinessUnit> getBusinessUnits(String country, String state, String city){
		
		List<BusinessUnit> buList = merchantRepository.getBusinessUnits(country, state, city);
		
		return buList;
	}
	
	public List<FoodCourt> getFoodcourts(Long buId ){
		
		return merchantRepository.getFoodcourts(buId);
	}

	public void saveMerchantMessageToAdmin(MerchantContactAdmin merchantContactAdmin) {
		
		merchantRepository.saveMerchantMessage(merchantContactAdmin);
	}
	
	public MerchantSettings getSettings(Long merchantId) {
		MerchantSettings settings = merchantRepository.getSettings(merchantId);
		
		if(Objects.isNull(settings)) {
			settings = new MerchantSettings();
			settings.setPrintType("Manual");
		}
		
		return settings;
	}
	
	public void saveSettings(Long merchantId, String key) {
		
		MerchantSettings existingSettings = this.getSettings(merchantId);
		
		if(Objects.isNull(existingSettings)) {
			existingSettings = new MerchantSettings();
		}
		
		if(key.equalsIgnoreCase("ADMIN_NOTIF")) {
			existingSettings.setAdminNotification(!existingSettings.isAdminNotification());
		}else if(key.equalsIgnoreCase("ORDER_NOTIF")) {
			existingSettings.setOrderNotifications(!existingSettings.isOrderNotifications());
		}else if(key.equalsIgnoreCase("REMAINDER_NOTIF")) {
			existingSettings.setRemainderNotifications(!existingSettings.isRemainderNotifications());
		}else if(key.equalsIgnoreCase("SUB_NOTIF")) {
			existingSettings.setSubscriptionNotifications(!existingSettings.isSubscriptionNotifications());
		}else if(key.equalsIgnoreCase("PRINT")) {
			existingSettings.setPrintType(existingSettings.getPrintType().equalsIgnoreCase("Manual") ? "Auto" : "Manual");
		}

		existingSettings.setMerchantId(merchantId);
		
		merchantRepository.saveSettings(existingSettings);
		
	}
}
