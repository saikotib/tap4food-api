package com.endeavour.tap4food.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.endeavour.tap4food.app.enums.UserStatusEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.repository.UserRepository;
import com.endeavour.tap4food.app.response.dto.CustomizationResponse;
import com.endeavour.tap4food.app.response.dto.ItemPatternPrice;
import com.endeavour.tap4food.app.security.model.User;
import com.endeavour.tap4food.app.util.CommonUtil;

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
		
		String message = String.format("%s is the OTP to login to your Tap4Food.please enter the OTP to verify your mobile number.", otp).replaceAll("\\s", "%20");
		
		commonService.sendSMS(phoneNumber, message);
		
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
			throw new TFException("This phone number is temporarily blocked.");
		}
		
		return otpMatch;		
	}
	
	public Otp fetchOtp(final String phoneNumber) {
		
		boolean otpMatch = false;
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		return otp;		
	}
	
	public List<FoodStall> getFoodStalls(Long foodCourtId){
		
		return userRepository.getFoodStalls(foodCourtId);
	}
	
	public Map<String, List<FoodItem>> getFoodItems(Long fsId){
		
		Map<String, List<FoodItem>> foodItemsMap = new HashMap<String, List<FoodItem>>();
		
		List<FoodItem> foodItems = userRepository.getFoodItems(fsId);
		List<FoodItem> recomendedFoodItems = new ArrayList<FoodItem>();
		List<FoodItem> vegItems = new ArrayList<FoodItem>();
		List<FoodItem> eggItems = new ArrayList<FoodItem>();
		
		for(FoodItem foodItem : foodItems) {
			
			String category = foodItem.getCategory();
			
			if(!foodItemsMap.containsKey(category)) {
				foodItemsMap.put(category, new ArrayList<FoodItem>());
			}
			
			List<FoodItem> categorisedFoodItemsList = foodItemsMap.get(category);
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
		foodItemsMap.put("recommended", recomendedFoodItems);
		
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
			
			customiseTypeDescriptionsMap.put(customizeItemTokens[0], customizeItemTokens[1]);
		}
		
		return customiseTypeDescriptionsMap;
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

		Map<String, String> descriptionsMap = this.getCustomiseTypeDescriptions(foodItemCustomiseDetails.getCustomiseFoodItemsDescriptions());
		Map<String, List<String>> customizeTypeWiseFoodItemsMap = this.getCustomizeTypeWiseFoodItems(foodItemCustomiseDetails.getCustomiseFoodItems());
		
		List<FoodItem> combinationItems = userRepository.getFoodItemCombinations(foodItemId);
		
		Map<String, FoodItem> combinationItemsMap = new HashMap<String, FoodItem>();
		
		for(FoodItem item : combinationItems) {
			item.setCombination(item.getCombination().replaceAll("##", "~"));
			combinationItemsMap.put(item.getCombination(), item);
		}
		
		String topKey = null;
		List<String> topKeyList = new ArrayList<String>();
		
		int order = 1;
		for(Map.Entry<String, String> entry : descriptionsMap.entrySet()) {
			CustomizationResponse.Option option = new CustomizationResponse.Option();
			
			option.setOrder(order);
			option.setKey(entry.getKey());
			option.setLabel(entry.getValue());
			option.setOptionItems(customizeTypeWiseFoodItemsMap.get(entry.getKey()));
			
			if(order == 1) {
				option.setPrices(new ArrayList<ItemPatternPrice>());
				
				topKey = entry.getKey();
				topKeyList = customizeTypeWiseFoodItemsMap.get(topKey);
				
			}else {
				List<ItemPatternPrice> itemPatternPrices = new ArrayList<ItemPatternPrice>();
				for(String topKeyItem : topKeyList) {
					
					List<String> custItems = customizeTypeWiseFoodItemsMap.get(entry.getKey());
					
					for(String customizeTypeWiseFoodItem : custItems) {

						String custType = topKeyItem + "~" +customizeTypeWiseFoodItem;
						
						FoodItem combinationItem = combinationItemsMap.get(custType);
						
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
}
