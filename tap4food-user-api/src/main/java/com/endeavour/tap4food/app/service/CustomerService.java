package com.endeavour.tap4food.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
	
	public boolean sendOTPToPhone(final String phoneNumber) {
		
		boolean flag = false;
		
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
		
		flag = true;
		return flag;		
	}
	
	public boolean verifyOTP(final String phoneNumber, final String inputOTP) {
		
		boolean otpMatch = false;
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		if(inputOTP.equalsIgnoreCase(otp.getOtp())) {
			otpMatch = true;
		}
		
		User user = new User();
		user.setPhoneNumber(phoneNumber);
		
		userRepository.save(user);
		
		
		return otpMatch;		
	}
	
	public Otp fetchOtp(final String phoneNumber) {
		
		boolean otpMatch = false;
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		return otp;		
	}
}
