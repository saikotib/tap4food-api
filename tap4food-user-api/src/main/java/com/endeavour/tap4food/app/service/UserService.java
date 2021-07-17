package com.endeavour.tap4food.app.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.repository.UserRepository;
import com.endeavour.tap4food.app.security.model.User;

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

	public boolean resentOtp(final String phoneNumber) {
		boolean flag = false;

		Optional<User> userData = userRepository.findByPhoneNumber(phoneNumber);

		if (userData.isPresent()) {

			if (commonService.sendOTPToPhone(phoneNumber)) {
				flag = true;
			}
		}

		return flag;
	}
}
