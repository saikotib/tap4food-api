package com.endeavour.tap4food.admin.app.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.validation.Valid;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.admin.app.enums.BusinessUnitEnum;
import com.endeavour.tap4food.admin.app.repository.AdminRepository;
import com.endeavour.tap4food.admin.app.response.dto.FoodCourtResponse;
import com.endeavour.tap4food.admin.app.response.dto.MerchantResponseDto;
import com.endeavour.tap4food.app.enums.AccountStatusEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.Access;
import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.AdminDashboardData;
import com.endeavour.tap4food.app.model.AdminDashboardData.MerchantRequests;
import com.endeavour.tap4food.app.model.AdminDashboardData.MerchantVsRevenue;
import com.endeavour.tap4food.app.model.AdminDashboardData.ReportParams;
import com.endeavour.tap4food.app.model.AdminDashboardData.Subscriptions;
import com.endeavour.tap4food.app.model.AdminRole;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.RoleConfiguration;
import com.endeavour.tap4food.app.model.Subscription;
import com.endeavour.tap4food.app.model.admin.AboutUs;
import com.endeavour.tap4food.app.model.admin.TermsNConditions;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.response.dto.MerchantFoodStall;
import com.endeavour.tap4food.app.service.CommonService;
import com.endeavour.tap4food.app.util.ActiveStatus;
import com.endeavour.tap4food.app.util.ApiURL;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.app.util.EmailTemplateConstants;
import com.endeavour.tap4food.app.util.MediaConstants;
import com.endeavour.tap4food.app.util.MongoCollectionConstant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@Autowired
	private AdminNextSequenceService adminNextSequenceService;

	@Value("${tap4food.merchant.api.url}")
	private String merchantApiBaseUrl;
	
	@Value("${images.server}")
	private String mediaServerUrl;

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

	public FoodStall updateMerchantStatus(final String status, final Long foodstallId, final Long merchantUniqueId) throws TFException {

		/*
		 * String merchantStatusUpdateApiUrl = merchantApiBaseUrl + "/update-status";
		 * 
		 * RestTemplate restTemplate = new RestTemplate();
		 * 
		 * HttpHeaders requestHeaders = new HttpHeaders(); requestHeaders.add("Accept",
		 * MediaType.APPLICATION_JSON_VALUE); HttpEntity requestEntity = new
		 * HttpEntity(requestHeaders);
		 * 
		 * UriComponentsBuilder uriBuilder =
		 * UriComponentsBuilder.fromHttpUrl(merchantStatusUpdateApiUrl)
		 * .queryParam("status", status).queryParam("uniqueNumber", merchantUniqueId);
		 * 
		 * ResponseEntity<ResponseHolder> responseEntity =
		 * restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.PUT,
		 * requestEntity, ResponseHolder.class);
		 * 
		 * System.out.println("Get Body : " + responseEntity.getBody());
		 * 
		 * return responseEntity.getBody();
		 */

		FoodStall foodstall = adminRepository.updateMerchantStatus(merchantUniqueId, foodstallId, status);

		return foodstall;
	}
	
	public FoodStall updateFoodstallStatus(final String status, final Long foodstallId, final Long merchantUniqueId) throws TFException {

		FoodStall foodstall = adminRepository.updateFoodstallStatus(merchantUniqueId, foodstallId, status);

		return foodstall;
	}

	public MerchantFoodStall createMerchant(Merchant merchant) {

		merchant.setStatus("Active");
		merchant.setPhoneNumberVerified(true);

		Long currentTimeInMilli = System.currentTimeMillis();

		merchant.setCreatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));
		merchant.setLastUpdatedDate(DateUtil.getDateFromMillisec(currentTimeInMilli));

		merchant.setPassword(encoder.encode(merchant.getPassword()));

		adminRepository.createMerchant(merchant);

		Optional<Merchant> merchantOptionalObject = adminRepository
				.findMerchantByPhoneNumber(merchant.getPhoneNumber());
		
		MerchantFoodStall merchantFoodStall = new MerchantFoodStall();

		if (merchantOptionalObject.isPresent()) {

			merchant = merchantOptionalObject.get();

			if (Objects.isNull(merchant.getUniqueNumber())) {
				
				Long uniqNumber = this.getUniqueNumber();

				merchant.setUniqueNumber(uniqNumber);
				
				merchantFoodStall.setMerchantId(merchant.getUniqueNumber());
				merchantFoodStall.setDate(merchant.getCreatedDate());
				merchantFoodStall.setOwner(merchant.getUserName());
				merchantFoodStall.setPhoneNumber(merchant.getPhoneNumber());
				merchantFoodStall.setStatus(AccountStatusEnum.ACTIVE.name());
				merchantFoodStall.setFoodStallName("");

				adminRepository.updateUniqueNumber(merchant);

				System.out.println("Unique number is updated forthe merchant...");

				String merchantEmail = merchant.getEmail();

				String createPasswordLink = ApiURL.API_URL + "/merchant/createPassword?uniqueNumber="
						+ uniqNumber;

				String message = commonService.getCreatePasswordHtmlContent()
						.replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(uniqNumber));

				String subject = "Tap4Food registration successfull";

//				commonService.sendEmail(merchantEmail, message, subject);
				
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

		return merchantFoodStall;
	}
	
	private Long getUniqueNumber() {

		Long uniqNumber = adminRepository.getRecentUniqueNumber();

		return uniqNumber;
	}

	public List<MerchantFoodStall> fetchMerchants() {

		Map<Long, Merchant> allMerchants = adminRepository.fetchMerchants();
		
		Map<Long, List<FoodStall>> allFoodStalls = adminRepository.getFoodStalls();
		
		List<MerchantFoodStall> merchantFoodStallInfoList = new ArrayList<MerchantFoodStall>();
		
		for(Merchant merchant : allMerchants.values()) {
			
//			merchantFoodStallInfo.setStatus(merchant.getStatus());
			
			if(allFoodStalls.containsKey(merchant.getUniqueNumber())) {
				for(FoodStall foodStall : allFoodStalls.get(merchant.getUniqueNumber())) {
					
					MerchantFoodStall merchantFoodStallInfo = new MerchantFoodStall();
					merchantFoodStallInfo.setMerchantId(merchant.getUniqueNumber());
					merchantFoodStallInfo.setDate(merchant.getCreatedDate());
					merchantFoodStallInfo.setFoodStallName("");
					merchantFoodStallInfo.setOwner(merchant.getUserName());
					merchantFoodStallInfo.setPhoneNumber(merchant.getPhoneNumber());
					merchantFoodStallInfo.setFoodStallName(foodStall.getFoodStallName());
					merchantFoodStallInfo.setFoodStallId(foodStall.getFoodStallId());
					
					if(StringUtils.hasText(foodStall.getStatus())) {
						if(foodStall.getStatus().equals(AccountStatusEnum.ACTIVE.getName())) {
							
							merchantFoodStallInfo.setStatus("Active");
						}else if(foodStall.getStatus().equals(AccountStatusEnum.INACTIVE.getName())) {
							
							merchantFoodStallInfo.setStatus("Inactive");
						}else {
							
							continue;
						}
					}else {
						continue;
					}
					
					String address = String.format("%s, %s, %s, %s", foodStall.getLocation(), foodStall.getCity(), foodStall.getState(), foodStall.getCountry());
					merchantFoodStallInfo.setAddress(address);
					
					merchantFoodStallInfoList.add(merchantFoodStallInfo);
				}
			}
		}

		Comparator<MerchantFoodStall> compareByUniqueNumber = new Comparator<MerchantFoodStall>() {
			@Override
			public int compare(MerchantFoodStall merchant1, MerchantFoodStall merchant2) {
				return merchant2.getMerchantId().compareTo(merchant1.getMerchantId());
			}
		};
		
		Collections.sort(merchantFoodStallInfoList, compareByUniqueNumber);
		
		return merchantFoodStallInfoList;
	}
	
	
	public List<MerchantFoodStall> fetchMerchantRequests() {

		Map<Long, Merchant> allMerchants = adminRepository.fetchMerchants();
		
		Map<Long, List<FoodStall>> allFoodStalls = adminRepository.getFoodStalls();
		
		List<MerchantFoodStall> merchantFoodStallInfoList = new ArrayList<MerchantFoodStall>();
		
		for(Merchant merchant : allMerchants.values()) {
			
			if(allFoodStalls.containsKey(merchant.getUniqueNumber())) {
				for(FoodStall foodStall : allFoodStalls.get(merchant.getUniqueNumber())) {
					
					MerchantFoodStall merchantFoodStallInfo = new MerchantFoodStall();
					merchantFoodStallInfo.setMerchantId(merchant.getUniqueNumber());
					
					merchantFoodStallInfo.setOwner(merchant.getUserName());
					merchantFoodStallInfo.setPhoneNumber(merchant.getPhoneNumber());
					
					merchantFoodStallInfo.setFoodStallName(foodStall.getFoodStallName());
					merchantFoodStallInfo.setDate(foodStall.getCreatedDate());
					merchantFoodStallInfo.setFoodStallId(foodStall.getFoodStallId());
					
					String address = String.format("%s, %s, %s, %s", foodStall.getLocation(), foodStall.getCity(), foodStall.getState(), foodStall.getCountry());
					merchantFoodStallInfo.setAddress(address);
					
					merchantFoodStallInfo.setCountry(foodStall.getCountry());
					merchantFoodStallInfo.setState(foodStall.getState());
					merchantFoodStallInfo.setCity(foodStall.getCity());
					merchantFoodStallInfo.setLocation(foodStall.getLocation());
					merchantFoodStallInfo.setLicenceNumber(foodStall.getFoodStallLicenseNumber());
					merchantFoodStallInfo.setGstNumber(foodStall.getGstNumber());
					merchantFoodStallInfo.setFcName(foodStall.getFoodCourtName());
					merchantFoodStallInfo.setBuType(foodStall.getBuType());
					merchantFoodStallInfo.setBuName(foodStall.getBuName());
					merchantFoodStallInfo.setDeliveryTime(foodStall.getDeliveryTime());
					
					if(Objects.isNull(foodStall.getQrCode())) {
						String qrCode = mediaServerUrl + "/QRCodes/" + foodStall.getFoodCourtId() + ".png";
						merchantFoodStallInfo.setFcQRCode(qrCode);
					}else {
						merchantFoodStallInfo.setFcQRCode(foodStall.getQrCode());
					}
					
					System.out.println("QR Code : " + merchantFoodStallInfo.getFcQRCode());
					
					merchantFoodStallInfo.setMenuPics(foodStall.getMenuPics());
					merchantFoodStallInfo.setStallPics(foodStall.getFoodStallPics());
					merchantFoodStallInfo.setEmail(merchant.getEmail());
					merchantFoodStallInfo.setUserName(merchant.getUserName());
					merchantFoodStallInfo.setPersonalIDNumber(merchant.getPersonalIdNumber());
					merchantFoodStallInfo.setPersonalIDUrl(merchant.getPersonalIdCard());
					merchantFoodStallInfo.setProfilePic(merchant.getProfilePic());
					
					System.out.println("merchant.getProfilePic() > " + merchant.getProfilePic());
					
					if(StringUtils.hasText(foodStall.getStatus())) {
						if(foodStall.getStatus().equalsIgnoreCase(AccountStatusEnum.SENT_FOR_APPROVAL.getName())) {
							
							merchantFoodStallInfo.setStatus("Open");
						}else if(foodStall.getStatus().equalsIgnoreCase(AccountStatusEnum.IN_PROGRESS.getName())) {
							
							merchantFoodStallInfo.setStatus("In Review");
						}else if(foodStall.getStatus().equalsIgnoreCase(AccountStatusEnum.ACTIVE.getName())) {
							
							merchantFoodStallInfo.setStatus("Active");
						}else if(foodStall.getStatus().equalsIgnoreCase(AccountStatusEnum.REJECTED.getName())) {
							
							merchantFoodStallInfo.setStatus("Rejected");
						}else if(foodStall.getStatus().equalsIgnoreCase(AccountStatusEnum.INACTIVE.getName())) {
							
							merchantFoodStallInfo.setStatus("Inactive");
						}else {
							merchantFoodStallInfo.setStatus("Open");
						}
					}else {
						merchantFoodStallInfo.setStatus("Open");
					}
					
					if(!merchantFoodStallInfo.getStatus().equalsIgnoreCase("Active") && !merchantFoodStallInfo.getStatus().equalsIgnoreCase("Inactive")) {
						merchantFoodStallInfoList.add(merchantFoodStallInfo);
					}
				}
			}
		}

		Comparator<MerchantFoodStall> compareByUniqueNumber = new Comparator<MerchantFoodStall>() {
			@Override
			public int compare(MerchantFoodStall merchant1, MerchantFoodStall merchant2) {
				return merchant2.getMerchantId().compareTo(merchant1.getMerchantId());
			}
		};
		
		Collections.sort(merchantFoodStallInfoList, compareByUniqueNumber);
		
		return merchantFoodStallInfoList;
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

			String createPasswordLink = ApiURL.API_URL + "/admin/createAdminPassword?uniqueNumber=ADMIN&userName="
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

	public BusinessUnit saveBusinessUnits(@Valid BusinessUnit businessUnit) {
		
		businessUnit.setBusinessUnitId(adminNextSequenceService.getNextSequence(BusinessUnit.SEQUENCE));
		
		return adminRepository.saveBusinessUnit(businessUnit);
	}

	public boolean deleteBusinessUnitById(final String businessUnitId) {

		return adminRepository.deleteBusinessUnitById(businessUnitId);
	}

	public Optional<List<BusinessUnit>> getBusinessUnits(Map<String, Object> filterMap) {
		return Optional.ofNullable(adminRepository.findBusinessUnitsByFilter(filterMap));
	}

	public Optional<BusinessUnit> uploadLogo(final Long buId, MultipartFile image) {
		Optional<BusinessUnit> businessUnit = adminRepository.findBusinessUnit(buId);
		
		Map<String, String> mediaMap = commonService.getAdminMediaDirs();
		
		String uploadPath = mediaMap.get(MediaConstants.GET_KEY_BUSINESS_UNITS_DIR_ADMIN);
		
		uploadPath = uploadPath.replaceAll(MediaConstants.IDENTIFIER_BUID, String.valueOf(buId));
		
		new File(uploadPath).mkdirs();

		if (businessUnit.isPresent()) {
			
			BusinessUnit bu = businessUnit.get();
			
			try {
				
				Path path = Paths.get(uploadPath);
				File existingFile = new File(uploadPath + File.separator + image.getOriginalFilename());
				
				if(existingFile.exists()) {
					if(existingFile.delete()) {
						log.info("Deleted the existing file");
					}
				}
				
				Files.copy(image.getInputStream(), path.resolve(image.getOriginalFilename()));
				
				String profilePicLink = uploadPath.replaceAll(commonService.getMediaBaseLocation(), "").replaceAll("\\\\", "/");
				
				profilePicLink = mediaServerUrl + profilePicLink + "/" + image.getOriginalFilename();
				
				log.info("profilePicLink :" + profilePicLink);
				
				bu.setLogo(profilePicLink);
				
				adminRepository.saveBusinessUnit(bu);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		return businessUnit;
	}

	public Optional<FoodCourt> addFoodCourt(final Long buId, FoodCourt foodCourt) throws TFException {
		Optional<BusinessUnit> businessUnit = adminRepository.findBusinessUnit(buId);

		if (businessUnit.isPresent()) {
			if (!businessUnit.get().getType().equals(BusinessUnitEnum.RESTAURANT)) {
				
				foodCourt.setBusinessUnitId(buId);
				foodCourt.setFoodCourtId(adminNextSequenceService.getNextSequence(FoodCourt.SEQ_NAME));
				foodCourt = adminRepository.saveFoodCourt(foodCourt);

			}

		}else {
			throw new TFException("Invalid business unit");
		}

		return Optional.ofNullable(foodCourt);
	}

	public Optional<FoodCourt> updateFoodCourt(final Long foodCourtId, FoodCourt foodCourt) {

		Optional<FoodCourt> foodCourtRes = adminRepository.findFoodCourt(foodCourtId);

		FoodCourt foodCourtObject = new FoodCourt();

		if (foodCourtRes.isPresent()) {

			foodCourtObject.setId(foodCourtRes.get().getId());
			foodCourtObject = foodCourt;
			adminRepository.saveFoodCourt(foodCourtObject);
		}

		return Optional.ofNullable(foodCourtObject);
	}

	public Optional<FoodCourt> uploadFoodCourtLogo(final Long foodCourtId, MultipartFile image) {
		Optional<FoodCourt> foodCourt = adminRepository.findFoodCourt(foodCourtId);
		
		String uploadPath = commonService.getAdminMediaDirs().get(MediaConstants.GET_KEY_FOODCOURTS_DIR_ADMIN).replaceAll(MediaConstants.IDENTIFIER_FCID, String.valueOf(foodCourtId));
		
		new File(uploadPath).mkdirs();

		if (foodCourt.isPresent()) {
			
			FoodCourt fc = foodCourt.get();
			
			try {
				
				Path path = Paths.get(uploadPath);
				File existingFile = new File(uploadPath + File.separator + image.getOriginalFilename());
				
				if(existingFile.exists()) {
					if(existingFile.delete()) {
						log.info("Deleted the existing file");
					}
				}
				
				Files.copy(image.getInputStream(), path.resolve(image.getOriginalFilename()));
				
				String profilePicLink = uploadPath.replaceAll(commonService.getMediaBaseLocation(), "").replaceAll("\\\\", "/");
				
				profilePicLink = mediaServerUrl + profilePicLink + "/" + image.getOriginalFilename();
				
				log.info("profilePicLink :" + profilePicLink);
				
				fc.setLogo(profilePicLink);
				
				adminRepository.saveFoodCourt(fc);

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		return foodCourt;
	}

	public boolean deleteFoodCourtId(@Valid Long foodCourtId) {
		boolean flag = false;
		Optional<FoodCourt> foodCourt = adminRepository.findFoodCourt(foodCourtId);

		if (foodCourt.isPresent()) {
			Optional<BusinessUnit> businessUnit = adminRepository
					.findBusinessUnit(foodCourt.get().getBusinessUnitId());
			
			BusinessUnit bu = businessUnit.get();

			adminRepository.deleteFoodCourtById(foodCourtId);

			flag = true;
		}

		return flag;
	}

	public Optional<FoodCourt> getFoodCourtById(Long foodCourtId) {
		return adminRepository.findFoodCourt(foodCourtId);
	}
	
	public List<FoodCourt> getFoodCourts(Long buId) {
		
		List<FoodCourt> foodCourts = adminRepository.getFoodCourts(buId);
		
		return foodCourts;
	}
	
	public List<FoodCourtResponse> getFoodCourts() {
		
		List<BusinessUnit> buList = adminRepository.findBusinessUnits();
		
		System.out.println("BULIST : " + buList);
		
		List<FoodCourtResponse> foodCourtsResponseList = new ArrayList<FoodCourtResponse>();
		
		for(BusinessUnit bu : buList) {
			
			List<FoodCourt> foodCourts = adminRepository.getFoodCourts(bu.getBusinessUnitId());
			
			if(Objects.nonNull(foodCourts)) {

				for(FoodCourt fc : foodCourts) {
					FoodCourtResponse foodCourtResponse = new FoodCourtResponse();
					
					foodCourtResponse.setBuName(bu.getName());
					foodCourtResponse.setCity(bu.getCity());
					foodCourtResponse.setCountry(bu.getCountry());
					foodCourtResponse.setState(bu.getState());
					
					foodCourtResponse.setFoodCourtName(fc.getName());
					foodCourtResponse.setQRCodeGenerated(fc.isQRCodeGenerated());
					foodCourtResponse.setQrCodeUrl(fc.getQrCodeUrl());
					foodCourtResponse.setFoodCourtId(fc.getFoodCourtId());
					
					foodCourtsResponseList.add(foodCourtResponse);
				}
			}
			
		}
		
		return foodCourtsResponseList;
	}

	public AdminDashboardData loadAdminDashboardData() {

		AdminDashboardData dashboard = new AdminDashboardData();
		dashboard.setShoppingMalls(26L);
		dashboard.setRestaurants(78L);
		dashboard.setTheaters(969L);
		dashboard.setTotalCustomers(8236L);
		dashboard.setTotalFoodCourts(50L);
		dashboard.setTotalFoodStalls(724L);
		dashboard.setTotalMerchants(610L);
		dashboard.setTotalOrders(16071L);

		ReportParams reportData = new ReportParams();
		reportData.setCustomers(170L);
		reportData.setFoodStalls(4L);
		reportData.setRestaurants(20L);

		Map<String, ReportParams> monthlyReportData = new HashMap<String, AdminDashboardData.ReportParams>();

		monthlyReportData.put("JAN", reportData);
		reportData.setCustomers(489L);
		reportData.setFoodStalls(61L);
		reportData.setRestaurants(778L);
		monthlyReportData.put("FEB", reportData);
		reportData.setCustomers(139L);
		reportData.setFoodStalls(8L);
		reportData.setRestaurants(37L);
		monthlyReportData.put("MAR", reportData);
		monthlyReportData.put("APR", reportData);
		reportData.setCustomers(139L);
		reportData.setFoodStalls(8L);
		reportData.setRestaurants(37L);
		monthlyReportData.put("MAY", reportData);
		monthlyReportData.put("JUN", reportData);
		reportData.setCustomers(699L);
		reportData.setFoodStalls(12L);
		reportData.setRestaurants(68L);
		monthlyReportData.put("JUL", reportData);
		monthlyReportData.put("AUG", reportData);

		Map<String, Map<String, ReportParams>> reportStatsMap = new HashMap<String, Map<String, ReportParams>>();

		reportStatsMap.put("2021", new HashMap<String, AdminDashboardData.ReportParams>());
		reportStatsMap.put("2021", monthlyReportData);

		dashboard.setReportMap(reportStatsMap);

		Map<String, Subscriptions> subscriptionsMap = new HashMap<String, AdminDashboardData.Subscriptions>();

		Subscriptions subscriptions = new Subscriptions();
		subscriptions.setExpired(10L);
		subscriptions.setNewSubscriptions(20L);
		subscriptions.setRenewal(30L);

		subscriptionsMap.put("JAN-2021", subscriptions);

		subscriptions.setExpired(16L);
		subscriptions.setNewSubscriptions(27L);
		subscriptions.setRenewal(25L);

		subscriptionsMap.put("FEB-2021", subscriptions);
		subscriptionsMap.put("MAR-2021", subscriptions);
		subscriptionsMap.put("APR-2021", subscriptions);
		subscriptionsMap.put("MAY-2021", subscriptions);
		subscriptionsMap.put("JUN-2021", subscriptions);
		subscriptionsMap.put("JUL-2021", subscriptions);

		dashboard.setSubscriptionsMap(subscriptionsMap);

		Map<String, MerchantRequests> merchantRequestsMap = new HashMap<String, AdminDashboardData.MerchantRequests>();

		MerchantRequests merchantRequests = new MerchantRequests();
		merchantRequests.setApproved(60L);
		merchantRequests.setInProgress(200L);
		merchantRequests.setOpen(12L);
		merchantRequests.setRejected(7L);

		merchantRequestsMap.put("JAN-2021", merchantRequests);
		merchantRequestsMap.put("FEB-2021", merchantRequests);
		merchantRequestsMap.put("MAR-2021", merchantRequests);
		merchantRequests.setApproved(80L);
		merchantRequests.setInProgress(500L);
		merchantRequests.setOpen(89L);
		merchantRequests.setRejected(7L);
		merchantRequestsMap.put("APR-2021", merchantRequests);
		merchantRequestsMap.put("MAY-2021", merchantRequests);
		merchantRequestsMap.put("JUN-2021", merchantRequests);
		merchantRequestsMap.put("JUL-2021", merchantRequests);

		dashboard.setMerchantRequestsMap(merchantRequestsMap);

		Map<String, MerchantVsRevenue> merchantVsRevenueMap = new HashMap<String, AdminDashboardData.MerchantVsRevenue>();
		MerchantVsRevenue merchantVsRevenue = new MerchantVsRevenue();
		merchantVsRevenue.setMerchants((double) 200);
		merchantVsRevenue.setRevenue((double) 15000);

		merchantVsRevenueMap.put("JAN-2021", merchantVsRevenue);
		merchantVsRevenueMap.put("FEB-2021", merchantVsRevenue);
		merchantVsRevenueMap.put("MAR-2021", merchantVsRevenue);
		merchantVsRevenueMap.put("APR-2021", merchantVsRevenue);
		merchantVsRevenueMap.put("MAY-2021", merchantVsRevenue);
		merchantVsRevenueMap.put("JUN-2021", merchantVsRevenue);
		merchantVsRevenueMap.put("JUL-2021", merchantVsRevenue);
		merchantVsRevenueMap.put("AUG-2021", merchantVsRevenue);

		dashboard.setMerchantVsRevenueMap(merchantVsRevenueMap);

		return dashboard;
	}

	public void correlateFCFS(Long foodCourtId, Long foodStallId) throws TFException {

		adminRepository.correlateFCFS(foodStallId, foodCourtId);
	}

	public AdminRole saveAdminRole(AdminRole adminRole) {

		return adminRepository.saveAdminRole(adminRole);
	}

	public List<AdminRole> getAdminRoles() {

		return adminRepository.findAdminRoles();
	}

	private Long getIdForAdminUserId() {

		Long adminUserId = adminNextSequenceService.getNextSequence(MongoCollectionConstant.COLLECTION_ADMINUSER_SEQ);

		return adminUserId;
	}

	public Admin saveAdminUser(Admin admin) throws TFException {

		AdminRole role = adminRepository.findRoleByRoleName(admin.getRole());

		if (!Objects.isNull(role)) {
			admin.setAdminUserId(getIdForAdminUserId());
			admin.setStatus(ActiveStatus.ACTIVE);
			admin = adminRepository.saveAdmin(admin);
		} else {

			throw new TFException("Role is not found");
		}

		return admin;
	}

	public List<Admin> getAdminUserByRole(final String role) {

		return adminRepository.findAdminUserByRole(role);
	}

	public Admin updateAdmin(Admin admin, final long adminUserId) throws TFException {

		Admin adminRes = adminRepository.finAdminByUserId(adminUserId);
		if (Objects.nonNull(adminRes)) {
			admin.setId(adminRes.getId());
			return adminRepository.saveAdmin(admin);
		} else {
			throw new TFException("Admin user not found");
		}

	}

	public Admin addAdminUserProfilePic(MultipartFile adminProfilePic, long adminUserId) throws TFException {
		Admin admin = adminRepository.finAdminByUserId(adminUserId);
		if (Objects.nonNull(admin)) {
			try {
				admin.setAdminUserProfilePic((new Binary(BsonBinarySubType.BINARY, adminProfilePic.getBytes())));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return adminRepository.saveAdmin(admin);
		} else {
			throw new TFException("Admin User not fount");
		}

	}

	public Boolean deleteAdminUser(long adminUserId) {

		Boolean flag = adminRepository.deleteAdminUserByRole(adminUserId);
		return flag;
	}

	public String changePassword(final String phoneNumber, final String oldPassword, final String newPassword)
			throws TFException {

		String message = null;

		Optional<Admin> adminData = adminRepository.findAdminByPhoneNumber(phoneNumber);

		if (adminData.isPresent()) {

			Admin admin = adminData.get();

			System.out.println("Is password matched :" + encoder.matches(oldPassword, admin.getPassword()));

			if (encoder.matches(oldPassword, admin.getPassword())) {

				adminRepository.changePassword(phoneNumber, encoder.encode(newPassword));

				message = "Password is changed successfully";

			} else {
				message = "Old password is incorrect";
			}
		} else {
			throw new TFException("Invalid Admin User Phone Number");
		}

		return message;
	}

	public RoleConfiguration saveAdminRoleConfiguration(String roleName, List<Access> accessDetails) {

		RoleConfiguration roleConfiguration = new RoleConfiguration();
		AdminRole adminRole = adminRepository.findRoleByRoleName(roleName);

		if (!Objects.isNull(adminRole)) {
			roleConfiguration.setRoleName(roleName);
			roleConfiguration.setAccessDetails(accessDetails);

			roleConfiguration = adminRepository.saveAdminRoleConfiguration(roleConfiguration);
		} else {
			roleConfiguration = null;
		}

		return roleConfiguration;
	}
	
	public AboutUs saveAboutUsData(AboutUs aboutUsData) {

		aboutUsData = adminRepository.saveAboutUsData(aboutUsData.getDescription());
		
		return aboutUsData;
	}

	public AboutUs getAboutUsData(){
		
		AboutUs data = adminRepository.getAboutUsData();
		
		return data;
	}
	
	public Subscription addSubscription(Subscription subscription) throws TFException {

		List<Subscription> existingSubscriptions = this.getExistingSubscriptions();
		
		for(Subscription existingSubscription : existingSubscriptions) {
			if(existingSubscription.getPlanName().equalsIgnoreCase(subscription.getPlanName())) {
				throw new TFException("The subscription is aleady exists.");
			}
		}
		
		subscription = adminRepository.addSubscription(subscription);
		
		return subscription;
	}
	
	@Bean
	public List<Subscription> getExistingSubscriptions(){
		
		return adminRepository.getExistingSubscriptions();
	}
	
	public TermsNConditions saveTermsAndConditions(String termsAndConditions) {
		
		TermsNConditions content = adminRepository.saveTermsAndConditions(termsAndConditions);
		
		return content;
	}
	
	public TermsNConditions getTermsAndConditions() {
		
		TermsNConditions content = adminRepository.getTermsAndConditions();
		
		if(Objects.isNull(content)) {
			content = new TermsNConditions();
		}
		
		return content;
	}
}
