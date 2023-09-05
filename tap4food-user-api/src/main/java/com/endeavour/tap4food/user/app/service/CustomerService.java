package com.endeavour.tap4food.user.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.enums.UserStatusEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.ContactUs;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.admin.AboutUs;
import com.endeavour.tap4food.app.model.admin.TermsNConditions;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemDto;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.model.fooditem.PreProcessedFoodItems;
import com.endeavour.tap4food.app.model.order.Order;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.response.dto.CustomizationResponse;
import com.endeavour.tap4food.app.response.dto.FoodCourtResponse;
import com.endeavour.tap4food.app.response.dto.ItemPatternPrice;
import com.endeavour.tap4food.app.service.CommonService;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.user.app.payload.request.ProfileUpdateRequest;
import com.endeavour.tap4food.user.app.repository.UserRepository;
import com.endeavour.tap4food.user.app.security.model.User;
import com.endeavour.tap4food.user.app.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomerService {
	
	@Autowired
	private CommonRepository commonRepository;
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderService orderService;
	
	private int otpValidTime = 5 * 60 * 1000;  // 5 mins in milliseconds
	
	private int blockReleasTime = 5 * 60 * 1000;  // 5 mins in milliseconds
	
	public boolean sendOTPToPhone(final String phoneNumber) throws TFException {
		
		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
		
		if(userData.isPresent()) {
			User user = userData.get();
			if(user.getStatus().equals(UserStatusEnum.LOCKED)) {
				if(commonService.getTimeDiff(user.getLockedTimeMs()) > blockReleasTime) {
					user.setStatus(UserStatusEnum.ACTIVE.name());
					userRepository.save(user);
				}else {
					throw new TFException("You phone number is blocked.");
				}
			}
		}
		
		String otp = CommonUtil.generateOTP();
		
		Otp otpObject = new Otp();
		otpObject.setIsExpired(false);
		otpObject.setOtp(otp);
		otpObject.setPhoneNumber(phoneNumber);
		otpObject.setIsExpired(false);
		otpObject.setOtpSentTimeInMs(System.currentTimeMillis());
		
		commonRepository.persistOTP(otpObject);
				
		//The SMS logic come here..
		
		String message = String.format("%s is the OTP to login to your Tap4Food.please enter the OTP to verify your mobile number. INTECH", otp).replaceAll("\\s", "%20");
		
		commonService.sendSMS(phoneNumber, message);
		
		log.info("The OTP generated : {}", otp);
		
		return true;		
	}
	
	public boolean sendOTPToPhone(final String phoneNumber, final Long orderId) throws TFException {
		
		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
				
		if(userData.isPresent()) {
			User user = userData.get();
			if(user.getStatus().equals(UserStatusEnum.LOCKED)) {
				if(commonService.getTimeDiff(user.getLockedTimeMs()) > blockReleasTime) {
					user.setStatus(UserStatusEnum.ACTIVE.name());
					userRepository.save(user);
				}else {
					throw new TFException("You phone number is blocked.");
				}
			}
		}
		Order order = orderService.getOrder(orderId);
		
		String otp = order.getOtp();
		
		Otp otpObject = new Otp();
		otpObject.setIsExpired(false);
		otpObject.setOtp(otp);
		otpObject.setPhoneNumber(phoneNumber);
		otpObject.setIsExpired(false);
		otpObject.setOtpSentTimeInMs(System.currentTimeMillis());
		
		commonRepository.persistOTP(otpObject);
				
		//The SMS logic come here..
		
//		String message = String.format("%s is the OTP to login to your Tap4Food.please enter the OTP to verify your mobile number. INTECH", otp).replaceAll("\\s", "%20");
		
//		commonService.sendSMS(phoneNumber, message);
		
		log.info("The OTP generated : {}", otp);
		
		return true;		
	}
	
	public void setOtpExpited(String phoneNumber) {
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		if(!Objects.isNull(otp)) {
			otp.setIsExpired(true);
			commonRepository.saveOtp(otp);
		}
	}
	
	public boolean verifyOTP(final String phoneNumber, final String inputOTP) throws TFException {
		
		boolean otpMatch = false;
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
		
		User user = new User();
		
		if(userData.isPresent()) {
			user = userData.get();
		}
		
		if(otp.getIsExpired()) {
			throw new TFException("OTP is expired");
		}
		
		if(inputOTP.equalsIgnoreCase(otp.getOtp())) {
			otpMatch = true;
			otp.setNumberOfTries(0);
			user.setStatus(UserStatusEnum.ACTIVE.name());
		}else {
			if(otp.getNumberOfTries() == null || otp.getNumberOfTries() == 0) {
				otp.setNumberOfTries(1);
			}else if(otp.getNumberOfTries() >= 1 && otp.getNumberOfTries() < 2) {
				otp.setNumberOfTries(otp.getNumberOfTries() + 1);
			}else if(otp.getNumberOfTries() == 2) {
				otp.setNumberOfTries(otp.getNumberOfTries() + 1);
				
				user.setLockedTimeMs(System.currentTimeMillis());
				user.setStatus(UserStatusEnum.LOCKED.name());	
				
			}
			otpMatch = false;
		}
		
		
		user.setPhoneNumber(phoneNumber);
		userRepository.save(user);
		otp.setOtp(otp.getOtp());
		
		commonRepository.saveOtp(otp);
		
		if(!ObjectUtils.isEmpty(user.getStatus()) && user.getStatus().equals(UserStatusEnum.LOCKED.name())) {
			throw new TFException("Temporarily blocked. Please retry after 5 mins.");
		}
		
		return otpMatch;		
	}
	
	public Otp fetchOtp(final String phoneNumber) {
		
		boolean otpMatch = false;
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		return otp;		
	}
	
	public List<FoodStall> getFoodStalls(Long foodCourtId, String timezone){
		
		List<FoodStall> foodstalls = userRepository.getFoodStalls(foodCourtId);
		
		List<FoodStall> responseFoodstalls = new ArrayList<FoodStall>();
		
		String today = DateUtil.todayName(timezone);
		
		for(FoodStall stall : foodstalls) {
			FoodStallTimings timings = userRepository.getFoodStallTimings(stall.getFoodStallId());
						
			if(!stall.getStatus().equalsIgnoreCase("Active")){
				stall.setOpened(false);
			}else if(ObjectUtils.isEmpty(timings.getDays())) {
				stall.setOpened(false);
			}else if(stall.isOpened()){
				for(WeekDay day : timings.getDays()) {
					
					if(day.getWeekDayName().equalsIgnoreCase(today)) {
						
						if(Objects.nonNull(day.getOpened24Hours()) && day.getOpened24Hours()) {
							stall.setOpened(true);
						}else if(Objects.nonNull(day.getClosed()) && day.getClosed()) {
							stall.setOpened(false);
						}else {
							
							
							String openTime = day.getOpenTime();
							String closeTime = day.getCloseTime();
							
							boolean stallOpenFlag = DateUtil.checkIfStallOpenedNow(openTime, closeTime, timezone);
							
							stall.setOpened(stallOpenFlag);
							
						}
					
						break;
					}			
					
				}
			}
			if(Objects.isNull(stall.getKeywords())) {
				stall.setKeywords(Collections.emptySet());
			}
			
			responseFoodstalls.add(stall);
		}
		
//		foodstalls = foodstalls
//				.stream()
//				.filter(stall -> stall.isOpened())
//				.collect(Collectors.toList());
		
		return foodstalls;
	}
	
	public FoodCourtResponse getFoodCourtDetails(Long foodCourtId){
		
		FoodCourtResponse response = new FoodCourtResponse();
		
		FoodCourt foodCourt = userRepository.getFoodCourt(foodCourtId);
		BusinessUnit bu = userRepository.getBusinessUnit(foodCourt.getBusinessUnitId());
		
		response.setAddress(bu.getAddress());
		response.setBuId(bu.getBusinessUnitId());
		response.setBuName(bu.getName());
		response.setBuType(bu.getType());
		response.setCity(bu.getCity());
		response.setCountry(bu.getCountry());
		response.setFoodCourtId(foodCourtId);
		response.setFoodCourtName(foodCourt.getName());
		response.setState(bu.getState());
		
		return response;
	}
	
	public Map<String, List<FoodItem>> getFoodItems_Parked(Long fsId){
		PreProcessedFoodItems preProcessedData = userRepository.getPreProcessedData(fsId);
		return preProcessedData.getFoodItemsMapByCategory();
	}
	
	
	
	public Map<String, List<FoodItem>> getFoodItems(Long fsId){
		
		Map<String, List<FoodItem>> foodItemsMap = new LinkedHashMap<String, List<FoodItem>>();
		
		List<FoodItem> foodItems = userRepository.getFoodItems(fsId);
		List<FoodItem> recomendedFoodItems = new ArrayList<FoodItem>();
		List<FoodItem> vegItems = new ArrayList<FoodItem>();
		List<FoodItem> eggItems = new ArrayList<FoodItem>();
		
		for(FoodItem foodItem : foodItems) {
			
			if(!StringUtils.hasText(foodItem.getTaxType())) {
				foodItem.setTaxType("E");
			}
						
			if(foodItem.getPrice() == null || foodItem.getPrice() == 0) {
				continue;
			}
			
			if("INACTIVE".equalsIgnoreCase(foodItem.getStatus())) {
				continue;
			}
			
			String category = foodItem.getCategory();
			String subcategory = foodItem.getSubCategory();
			
			String categoryAndSubCategory = category;
			
			if(!category.equalsIgnoreCase(subcategory)) {
				categoryAndSubCategory = category + " - " + subcategory;
			}
			
			if(!foodItemsMap.containsKey(categoryAndSubCategory)) {
				foodItemsMap.put(categoryAndSubCategory, new ArrayList<FoodItem>());
			}
			
			List<FoodItem> categorisedFoodItemsList = foodItemsMap.get(categoryAndSubCategory);
			categorisedFoodItemsList.add(foodItem);
			if(foodItem.isReccommended()) {
				recomendedFoodItems.add(foodItem);
			}
			if(foodItem.isVeg()) {
				vegItems.add(foodItem);
			}
			if(foodItem.isEgg()) {
				eggItems.add(foodItem);
			}
			
		}
		
		foodItemsMap.put("veg", vegItems);
		foodItemsMap.put("egg", eggItems);
		foodItemsMap.put("Recommended", recomendedFoodItems);
		
		return foodItemsMap;
	}
	
	public FoodItem getFoodItemDetails(Long foodItemId) {
		
		FoodItem foodItem = userRepository.getFoodItem(foodItemId);
		
		return foodItem;
		
	}
	
	private Map<String, String> getCustomiseTypeDescriptions(List<String> rawDescriptions){
	
		Map<String, String> customiseTypeDescriptionsMap = new LinkedHashMap<String, String>();

		for(String customizeTypeEntry : rawDescriptions) {
			String customizeItemTokens[] = customizeTypeEntry.split("~");
			if(customizeItemTokens.length == 2) {
				customiseTypeDescriptionsMap.put(customizeItemTokens[0], customizeItemTokens[1]);
			}else {
				customiseTypeDescriptionsMap.put(customizeItemTokens[0], "");
			}
			
		}
		
		return customiseTypeDescriptionsMap;
	}
	
	private Map<String, String> getCustomiseFoodItemsSelectButtons(List<String> rawSelectionOptions){
		
		Map<String, String> customiseFoodItemsSelectButtonsMap = new LinkedHashMap<String, String>();

		for(String customizeTypeEntry : rawSelectionOptions) {
			String customizeItemTokens[] = customizeTypeEntry.split("~");
			
			customiseFoodItemsSelectButtonsMap.put(customizeItemTokens[0], customizeItemTokens[1]);
		}
		
		return customiseFoodItemsSelectButtonsMap;
	}
	
	
	private Map<String, List<String>> getCustomizeTypeWiseFoodItems(List<String> customiseFoodItems){
		
		Map<String, List<String>> customizeTypeWiseFoodItemsMap = new LinkedHashMap<String, List<String>>();

		for(String customizeTypeEntry : customiseFoodItems) {
			String customizeItemTokens[] = customizeTypeEntry.split("~");
			
			if(!customizeTypeWiseFoodItemsMap.containsKey(customizeItemTokens[0])) {
				customizeTypeWiseFoodItemsMap.put(customizeItemTokens[0], new ArrayList<String>());
			}
			
			List<String> combinationsList = new ArrayList<String>();
			
			customizeTypeWiseFoodItemsMap.get(customizeItemTokens[0]).add(customizeItemTokens[1]);
		}
		
		return customizeTypeWiseFoodItemsMap;
	}
	
	public CustomizationResponse getCombinationResponse(Long foodItemId) {
		
		CustomizationResponse customizationResponse = new CustomizationResponse();
		
		List<CustomizationResponse.Option> options = new ArrayList<CustomizationResponse.Option>();
			
		customizationResponse.setFoodItemId(foodItemId);
		
		FoodItemCustomiseDetails foodItemCustomiseDetails = userRepository.getFoodItemCustomDetails(foodItemId);
		
		List<String> customiseTypes = foodItemCustomiseDetails.getCustomiseTypes();

		Map<String, String> descriptionsMap = this.getCustomiseTypeDescriptions(foodItemCustomiseDetails.getCustomiseFoodItemsDescriptions());
		Map<String, List<String>> customizeTypeWiseFoodItemsMap = this.getCustomizeTypeWiseFoodItems(foodItemCustomiseDetails.getCustomiseFoodItems());
		
		Map<String, String> selectButtonsMap = this.getCustomiseFoodItemsSelectButtons(foodItemCustomiseDetails.getCustomiseFoodItemsSelectButtons());
		
		List<FoodItem> combinationItems = userRepository.getFoodItemCombinations(foodItemId);
		
		Map<String, FoodItem> combinationItemsMap = new HashMap<String, FoodItem>();
		
		boolean isPizza = false;
		
		for(FoodItem item : combinationItems) {
			item.setCombination(item.getCombination().replaceAll("##", "~"));
			combinationItemsMap.put(item.getCombination(), item);
			
			if(!isPizza && item.isPizza()) {
				isPizza = true;
			}
		}
		
		String topKey = null;
		List<String> topKeyList = new ArrayList<String>();
		
		int order = 1;
//		for(Map.Entry<String, String> entry : descriptionsMap.entrySet()) {
		for(String custTypeKey : customiseTypes) {
			CustomizationResponse.Option option = new CustomizationResponse.Option();
			
			option.setOrder(order);
			option.setKey(custTypeKey);
			option.setLabel(descriptionsMap.get(custTypeKey));
			option.setOptionItems(customizeTypeWiseFoodItemsMap.get(custTypeKey));
			
			String buttonType = selectButtonsMap.get(custTypeKey);
			
			if(buttonType.equalsIgnoreCase("single")) {
				option.setMulti(false);
			}else {
				option.setMulti(true);
			}
			
			if(order == 1) {
				
				List<ItemPatternPrice> itemPatternPrices = new ArrayList<ItemPatternPrice>();
				
				if(!isPizza) {
					for(String combination : customizeTypeWiseFoodItemsMap.get(custTypeKey)) {
						
						System.out.println("combinationItemsMap > " + combinationItemsMap.keySet());
						System.out.println("combination > " + combination);
						FoodItem combinationItem = combinationItemsMap.get(combination);
						
						if(Objects.isNull(combinationItem)) {
							continue;
						}
						
						ItemPatternPrice itemPatternPrice = new ItemPatternPrice();
						itemPatternPrice.setPattern(combination);
						itemPatternPrice.setPrice(combinationItem.getPrice());
						
						itemPatternPrices.add(itemPatternPrice);
					}
				}
				
				option.setPrices(itemPatternPrices);
				
				topKey = custTypeKey;
				topKeyList = customizeTypeWiseFoodItemsMap.get(topKey);
				
			}else {
				List<ItemPatternPrice> itemPatternPrices = new ArrayList<ItemPatternPrice>();
				for(String topKeyItem : topKeyList) {
					
					List<String> custItems = customizeTypeWiseFoodItemsMap.get(custTypeKey);
					
					for(String customizeTypeWiseFoodItem : custItems) {

						String custType = topKeyItem + "~" +customizeTypeWiseFoodItem;
						
						FoodItem combinationItem = combinationItemsMap.get(custType);
						
						if(Objects.isNull(combinationItem)) {
							continue;
						}
						
						FoodItemPricing pricingInfo = userRepository.getCombinationPrices(combinationItem.getFoodItemId());

						ItemPatternPrice price = new ItemPatternPrice();
						price.setPattern(custType);
						price.setPrice(pricingInfo.getPrice());
						
						itemPatternPrices.add(price);
					}
				}
				
				option.setPrices(itemPatternPrices);
			}
			
			options.add(option);
			
			order++;
		}
		
		customizationResponse.setOptions(options);
		
		//Getting prices fro combinations.. START
		/*
		Map<String, List<String>> combinationPatternsMap = new LinkedHashMap<String, List<String>>();
		
		
		int counter = 0;
		
		for(Map.Entry<String, List<String>> entry : customizeTypeWiseFoodItemsMap.entrySet()) {

			String key = entry.getKey();
			List<String> valueList = entry.getValue();
		
			if(counter == 0) {
				topKey = key;
				topKeyList = new ArrayList<String>(valueList);
			}else {
				String keyPatten = topKey + "~" + key;
				List<String> patternsList = new ArrayList<String>();
				for(String topCustItem : topKeyList) {
					for(String custItem : valueList) {
						String combinationPattern = topCustItem + "##" + custItem;
						patternsList.add(combinationPattern);
					}
				}
				
				combinationPatternsMap.put(keyPatten, patternsList);
			}
			counter ++;
		}
		
		System.out.println("combinationPatternsMap >>" + combinationPatternsMap);
		
		List<FoodItem> combinationItems = userRepository.getFoodItemCombinations(foodItemId);
		
		Map<String, FoodItem> combinationItemsMap = new HashMap<String, FoodItem>();
		
		Map<String, Map<String, Map<String, Double>>> combinationsMap = new LinkedHashMap<String, Map<String, Map<String, Double>>>();
		
		for(FoodItem item : combinationItems) {
			combinationItemsMap.put(item.getCombination(), item);
		}
		
		for(Map.Entry<String, List<String>> entry : combinationPatternsMap.entrySet()) {
			String keyPattern = entry.getKey();
			List<String> patterns = entry.getValue();
			
			Map<String, Map<String, Double>> combinationsChildMap = new LinkedHashMap<String, Map<String,Double>>();
			
			for(String pattern : patterns) {
				FoodItem item = combinationItemsMap.get(pattern);
				FoodItemPricing pricingInfo = userRepository.getCombinationPrices(item.getFoodItemId());
				
				String combination = item.getCombination();
				String combinationTokens[] = combination.split("##");
				
				if(!combinationsChildMap.containsKey(combinationTokens[0])) {
					combinationsChildMap.put(combinationTokens[0], new HashMap<String, Double>());
				}
				
				combinationsChildMap.get(combinationTokens[0]).put(combinationTokens[1], pricingInfo.getCombinationPrice());
			}
			
			
			combinationsMap.put(keyPattern, combinationsChildMap);
		}
		
		customizationResponse.setCombinationsMap(combinationsMap);
		*/
		//END
		
		return customizationResponse;
	}
	
	public List<FoodItem> getFoodItemCombinationDetails(Long foodItemId) {
		List<FoodItem> combinationItems = userRepository.getFoodItemCombinations(foodItemId);
		
		List<FoodItem> formatedCombinationItems = new ArrayList<FoodItem>();
		
		for(FoodItem foodItem : combinationItems) {
			foodItem.setCombination(foodItem.getCombination().replaceAll("##", " "));
			
			formatedCombinationItems.add(foodItem);
		}
		
		return formatedCombinationItems;
	}
	
	public void getCombinationPrices(Long foodItemId, List<String> selectedCustTypes) {
		
		List<FoodItem> combinationItems = userRepository.getFoodItemCombinations(foodItemId);
		
		Map<String, Map<String, Double>> topCustTypeMap = new LinkedHashMap<String, Map<String,Double>>();
		
		for(FoodItem item : combinationItems) {
			FoodItemPricing pricingInfo = userRepository.getCombinationPrices(item.getFoodItemId());
			
			
		}
	}
	
	public User updateProfile(ProfileUpdateRequest request, String phoneNumber) throws TFException {
		
		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
		
		if(userData.isPresent()) {
			User user = userData.get();
			
			user.setEmail(request.getEmail());
			user.setFullName(request.getFullName());
			
			userRepository.save(user);
			
			return user;
		}else {
			throw new TFException("No user found with the given phone number");
		}	
		
	}
	
	public User getUserData(String phoneNumber) throws TFException {
		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
		
		if(userData.isPresent()) {
			
			return userData.get();
		} else {
			throw new TFException("No user found with the given phone number");
		}
	}
	
	public List<FoodItem> getFoodItemSuggesions(Set<Long> foodItemIdSet){
		
		Set<FoodItem> foodItems = new HashSet<FoodItem>();
		
		for(Long foodItemId : foodItemIdSet) {
			FoodItemCustomiseDetails customiseDetails = userRepository.getFoodItemCustomDetails(foodItemId);
			
			
			if(Objects.isNull(customiseDetails)) {
				continue;
			}
			List<String> suggestionItemIds = customiseDetails.getAddOnItemsIds();
			
			if(Objects.nonNull(suggestionItemIds)) {
				
				for(String itemId : suggestionItemIds) {
					FoodItem item = userRepository.getFoodItem(Long.valueOf(itemId));
					
					foodItems.add(item);
				}
			}			
		}	
		
		return new ArrayList<FoodItem>(foodItems);
	}
	
	public void submitContactUsForm(ContactUs form) {
		
		userRepository.submitContactUsForm(form);
	}
	
	public FoodStall getFoodStall(Long fsId, String timezone) {
		FoodStall stall = getFoodStall(fsId);
		
		String today = DateUtil.todayName(timezone);
		
		System.out.println(today);
		
		FoodStallTimings timings = userRepository.getFoodStallTimings(stall.getFoodStallId());
		
		if(!stall.getStatus().equalsIgnoreCase("Active")){
			stall.setOpened(false);
		}else if(ObjectUtils.isEmpty(timings.getDays())) {
			stall.setOpened(false);
		}else if(stall.isOpened()){
			for(WeekDay day : timings.getDays()) {
				
				if(day.getWeekDayName().equalsIgnoreCase(today)) {
					
					if(Objects.nonNull(day.getOpened24Hours()) && day.getOpened24Hours()) {
						stall.setOpened(true);
					}else if(Objects.nonNull(day.getClosed()) && day.getClosed()) {
						stall.setOpened(false);
					}else {
						
						
						String openTime = day.getOpenTime();
						String closeTime = day.getCloseTime();
						
						System.out.println(openTime);
						System.out.println(closeTime);
						
						boolean stallOpenFlag = DateUtil.checkIfStallOpenedNow(openTime, closeTime, timezone);
						
						stall.setOpened(stallOpenFlag);
						
					}
				
					break;
				}			
				
			}
		}
		
		return stall;
	}
	
	public FoodStall getFoodStall(Long fsId) {
		
		return userRepository.getFoodStall(fsId);
	}
	
	public AboutUs getAboutData() {
		
		return userRepository.getAboutUsContent();
	}
	
	public TermsNConditions getTnC() {
		
		return userRepository.getTnC();
	}
	
	public Set<String> getSuggestionList(Long fcId) {
		
		Set<String> suggestions = new HashSet<String>();
		
		List<FoodStall> stalls = userRepository.getFoodStalls(fcId);
		
		stalls.forEach(stall -> {
			List<FoodItem> items = userRepository.getFoodItems(stall.getFoodStallId());
			items.forEach(item -> {
				suggestions.add(item.getFoodItemName());
			});
		});
		
		return suggestions;
	}

	public List<Binary> getImageBytesById(Long foodItemId) {
		FoodItem foodItems = userRepository.getFoodItem(foodItemId);
		return foodItems.getPic();
	}
	
	 public Map<String, List<FoodItemDto>> getFoodItemsMapped(Long fsId) {
	        Map<String, List<FoodItem>> foodItemsMap = getFoodItemsWithoutpic(fsId);

	        Map<String, List<FoodItemDto>> foodItemsDtoMap = new HashMap<>();

	        foodItemsMap.forEach((key, foodItemList) -> {
	            List<FoodItemDto> foodItemDtoList = foodItemList.stream()
	                    .map(this::mapFoodItemToDto)
	                    .collect(Collectors.toList());

	            foodItemsDtoMap.put(key, foodItemDtoList);
	        });

	        return foodItemsDtoMap;
	    }
	 private FoodItemDto mapFoodItemToDto(FoodItem foodItem) {
		    return FoodItemDto.builder()
		            .id(foodItem.getId())
		            .foodItemId(foodItem.getFoodItemId())
		            .foodItemName(foodItem.getFoodItemName())
		            .description(foodItem.getDescription())
		            .category(foodItem.getCategory())
		            .subCategory(foodItem.getSubCategory())
		            .cuisine(foodItem.getCuisine())
		            .isAddOn(foodItem.isAddOn())
		            .isVeg(foodItem.isVeg())
		            .isEgg(foodItem.isEgg())
		            .isNonVeg(foodItem.isNonVeg())
		            .isReccommended(foodItem.isReccommended())
		            .isPizza(foodItem.isPizza())
		            .foodStallId(foodItem.getFoodStallId())
		            .rating(foodItem.getRating())
		            .totalReviews(foodItem.getTotalReviews())
		            .baseItem(foodItem.getBaseItem())
		            .combination(foodItem.getCombination())
		            .requestId(foodItem.getRequestId())
		            .availableCustomisation(foodItem.isAvailableCustomisation())
		            .addOns(foodItem.getAddOns())
		            .price(foodItem.getPrice())
		            .isDefaultCombination(foodItem.isDefaultCombination())
		            .status(foodItem.getStatus())
		            .taxType(foodItem.getTaxType())
		            .build();
		}
	 
	 
	 public Map<String, List<FoodItem>> getFoodItemsWithoutpic(Long fsId){
			
			Map<String, List<FoodItem>> foodItemsMap = new LinkedHashMap<String, List<FoodItem>>();
			
			List<FoodItem> foodItems = userRepository.getFoodItemsExcludepic(fsId);
			List<FoodItem> recomendedFoodItems = new ArrayList<FoodItem>();
			List<FoodItem> vegItems = new ArrayList<FoodItem>();
			List<FoodItem> eggItems = new ArrayList<FoodItem>();
			
			for(FoodItem foodItem : foodItems) {
				
				if(!StringUtils.hasText(foodItem.getTaxType())) {
					foodItem.setTaxType("E");
				}
							
				if(foodItem.getPrice() == null || foodItem.getPrice() == 0) {
					continue;
				}
				
				if("INACTIVE".equalsIgnoreCase(foodItem.getStatus())) {
					continue;
				}
				
				String category = foodItem.getCategory();
				String subcategory = foodItem.getSubCategory();
				
				String categoryAndSubCategory = category;
				
				if(!category.equalsIgnoreCase(subcategory)) {
					categoryAndSubCategory = category + " - " + subcategory;
				}
				
				if(!foodItemsMap.containsKey(categoryAndSubCategory)) {
					foodItemsMap.put(categoryAndSubCategory, new ArrayList<FoodItem>());
				}
				
				List<FoodItem> categorisedFoodItemsList = foodItemsMap.get(categoryAndSubCategory);
				categorisedFoodItemsList.add(foodItem);
				if(foodItem.isReccommended()) {
					recomendedFoodItems.add(foodItem);
				}
				if(foodItem.isVeg()) {
					vegItems.add(foodItem);
				}
				if(foodItem.isEgg()) {
					eggItems.add(foodItem);
				}
				
			}
			
			foodItemsMap.put("veg", vegItems);
			foodItemsMap.put("egg", eggItems);
			foodItemsMap.put("Recommended", recomendedFoodItems);
			
			return foodItemsMap;
		}
}
