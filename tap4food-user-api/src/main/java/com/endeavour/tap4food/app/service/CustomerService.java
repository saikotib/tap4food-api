package com.endeavour.tap4food.app.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.endeavour.tap4food.app.enums.UserStatusEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.repository.UserRepository;
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
	
	public boolean sendOTPToPhone(final String phoneNumber) throws TFException {
		
		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
		
		if(userData.isPresent()) {
			User user = userData.get();
			if(user.getStatus().equals(UserStatusEnum.LOCKED)) {
				throw new TFException("You phone number is blocked.");
			}
		}
		
		String otp = CommonUtil.generateOTP();
		
		Otp otpObject = new Otp();
		otpObject.setIsExpired(false);
		otpObject.setOtp(otp);
		otpObject.setPhoneNumber(phoneNumber);
		
		commonRepository.persistOTP(otpObject);
				
		//The SMS logic come here..
		
		String message = String.format("%s is the OTP to login to your Tap4Food.please enter the OTP to verify your mobile number.", otp).replaceAll("\\s", "%20");
		
		commonService.sendSMS(phoneNumber, message);
		
		log.info("The OTP generated : {}", otp);
		
		return true;		
	}
	
	public boolean verifyOTP(final String phoneNumber, final String inputOTP) throws TFException {
		
		boolean otpMatch = false;
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
		
		User user = new User();
		
		if(userData.isPresent()) {
			user = userData.get();
		}
		
		if(inputOTP.equalsIgnoreCase(otp.getOtp())) {
			otpMatch = true;
			otp.setNumberOfTries(0);
			user.setStatus(UserStatusEnum.ACTIVE.name());
		}else {
			if(otp.getNumberOfTries() == null) {
				otp.setNumberOfTries(1);
			}else if(otp.getNumberOfTries() >= 1 && otp.getNumberOfTries() < 3) {
				otp.setNumberOfTries(otp.getNumberOfTries() + 1);
			}else if(otp.getNumberOfTries() == 3) {
				otp.setNumberOfTries(otp.getNumberOfTries() + 1);
		
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
}
