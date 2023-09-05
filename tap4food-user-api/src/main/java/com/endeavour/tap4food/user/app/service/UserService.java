package com.endeavour.tap4food.user.app.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.enums.UserStatusEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.service.CommonService;
import com.endeavour.tap4food.user.app.repository.UserRepository;
import com.endeavour.tap4food.user.app.security.model.User;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CommonService commonService;

	public Optional<User> findByEmailId(String emailId) {

		return userRepository.findByEmailId(emailId);
	}

	public Optional<User> findByPhoneNumber(String phoneNumber) {

		return userRepository.findByPhoneNumber(phoneNumber);
	}

	public void saveUser(User user) {
		
		boolean isUserSaved = userRepository.save(user);

		if (isUserSaved) {
			commonService.sendOTPToPhone(user.getPhoneNumber());
		}
	}

	public String validateSignupData(final String emailId, final String phoneNumber) {

		String validationMessage = null;

		Optional<User> userByEmail = this.findByEmailId(emailId);

		if (userByEmail.isPresent()) {
			validationMessage = "The email id is already used.";

			return validationMessage;
		}

		Optional<User> userByPhOptional = this.findByPhoneNumber(phoneNumber);

		if (userByPhOptional.isPresent()) {
			validationMessage = "The phone number is already used.";

			return validationMessage;
		}
		
		return null;

	}

	public void resendOtp(final String phoneNumber) throws TFException {

		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
		
		User user = new User();
		
		if(userData.isPresent()) {
			user = userData.get();
			if(user.getStatus().equalsIgnoreCase(UserStatusEnum.LOCKED.name())) {
				throw new TFException("Your phone number is temporarily locked");
			}
		}else {
			user.setPhoneNumber(phoneNumber);
			user.setStatus(UserStatusEnum.ACTIVE.name());
			
			userRepository.save(user);
		}
		
		commonService.sendOTPToPhone(phoneNumber);

	}
	
	public void resendOtp(final String phoneNumber, Long orderId) throws TFException {

		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);
		
		User user = new User();
		
		if(userData.isPresent()) {
			user = userData.get();
			if(user.getStatus().equalsIgnoreCase(UserStatusEnum.LOCKED.name())) {
				throw new TFException("Your phone number is temporarily locked");
			}
		}else {
			user.setPhoneNumber(phoneNumber);
			user.setStatus(UserStatusEnum.ACTIVE.name());
			
			userRepository.save(user);
		}
		
		commonService.sendOTPToPhone(phoneNumber);

	}
}
