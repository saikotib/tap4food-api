package com.endeavour.tap4food.app.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.AdminRepository;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.app.util.EmailTemplateConstants;

@Service
public class AdminService {

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private CommonService commonService;

	@Autowired
	private CommonRepository commonRepository;

	@Autowired
	private PasswordEncoder encoder;

	@Value("${tap4food.merchant.api.url}")
	private String merchantApiBaseUrl;

	public boolean isMerchantFoundByEmail(final String merchantEmail) {
		Optional<Merchant> merchant = adminRepository.findMerchantByEmail(merchantEmail);

		if (merchant.isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	public Optional<Admin> findAdminUserByEmail(final String adminUserEmail) {
		Optional<Admin> admin = adminRepository.findAdminByEmailId(adminUserEmail);

		return admin;
	}
	
	public Optional<Admin> findAdminUserByUserName(final String userName) {
		Optional<Admin> admin = adminRepository.findAdminByUserName(userName);

		return admin;
	}
	
	public Optional<Admin> findAdminUserByPhoneNumber(final String adminPhoneNumber) {
		Optional<Admin> admin = adminRepository.findAdminByPhoneNumber(adminPhoneNumber);

		return admin;
	}

	public boolean isMerchantFoundByPhoneNumber(final String merchantPhoneNumber) {
		Optional<Merchant> merchant = adminRepository.findMerchantByPhoneNumber(merchantPhoneNumber);

		if (merchant.isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	public ResponseHolder updateMerchantStatus(final String status, final Long merchantUniqueId) {

		/*
		String merchantStatusUpdateApiUrl = merchantApiBaseUrl + "/update-status";

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity requestEntity = new HttpEntity(requestHeaders);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(merchantStatusUpdateApiUrl)
				.queryParam("status", status).queryParam("uniqueNumber", merchantUniqueId);

		ResponseEntity<ResponseHolder> responseEntity = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.PUT,
				requestEntity, ResponseHolder.class);

		System.out.println("Get Body : " + responseEntity.getBody());

		return responseEntity.getBody();
		*/
		
		return null;
	}

	public Merchant createMerchant(Merchant merchant) {

		merchant.setStatus("Active");

		Long currentTimeInMilli = System.currentTimeMillis();

		merchant.setCreatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));
		merchant.setLastUpdatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));

		merchant.setPassword(encoder.encode(merchant.getPassword()));

		adminRepository.createMerchant(merchant);

		Optional<Merchant> merchantOptionalObject = adminRepository
				.findMerchantByPhoneNumber(merchant.getPhoneNumber());

		if (merchantOptionalObject.isPresent()) {

			merchant = merchantOptionalObject.get();

			if (Objects.isNull(merchant.getUniqueNumber())) {

				Long uniqNumber = this.getUniqueNumber();

				merchant.setUniqueNumber(uniqNumber);

				adminRepository.updateUniqueNumber(merchant);

				System.out.println("Unique number is updated forthe merchant...");

				String merchantEmail = merchant.getEmail();

				String createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/merchant/createPassword?uniqueNumber="
						+ uniqNumber;

				String message = commonService.getCreatePasswordHtmlContent()
						.replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(uniqNumber));

				String subject = "Tap4Food registration successfull";

				commonService.sendEmail(merchantEmail, message, subject);
			}
		}

		return merchant;
	}

	private Long getUniqueNumber() {

		Long uniqNumber = adminRepository.getRecentUniqueNumber();

		return uniqNumber;
	}

	public List<Merchant> fetchMerchants() {

		List<Merchant> allMerchants = adminRepository.fetchMerchants();
		
		Comparator<Merchant> compareByUniqueNumber = new Comparator<Merchant>() {
		    @Override
		    public int compare(Merchant merchant1, Merchant merchant2) {
		        return merchant2.getUniqueNumber().compareTo(merchant1.getUniqueNumber());
		    }
		};
		
		Collections.sort(allMerchants, compareByUniqueNumber);
		
		return allMerchants;
	}

	public boolean verifyOTP(final String phoneNumber, final String inputOTP) {

		boolean otpMatch = false;

		Otp otp = commonRepository.getRecentOtp(phoneNumber);

		if (inputOTP.equalsIgnoreCase(otp.getOtp())) {
			otpMatch = true;
		} else {
			return otpMatch;
		}

		Optional<Admin> adminUserData = adminRepository.findAdminByPhoneNumber(phoneNumber);

		if (adminUserData.isPresent()) {
			Admin adminUser = adminUserData.get();

			String adminEmail = adminUser.getEmail();

			String createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/admin/createAdminPassword?uniqueNumber=ADMIN&userName="
					+ adminUser.getUserName();

			String message = commonService.getResetPasswordHtmlContent()
					.replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
					.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, adminUser.getUserName().toUpperCase());

			String subject = "Tap4Food Admin Password Reset";

			sendMail(adminEmail, message, subject);
		}

		return otpMatch;
	}

	public boolean createPassword(final String userName, final String password) {

		adminRepository.createAdminPassword(userName, password);

		return true;
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
}
