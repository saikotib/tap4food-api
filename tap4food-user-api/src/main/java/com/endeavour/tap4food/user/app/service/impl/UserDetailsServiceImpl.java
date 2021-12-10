package com.endeavour.tap4food.user.app.service.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.user.app.enums.UserRoleEnum;
import com.endeavour.tap4food.user.app.repository.UserRepository;
import com.endeavour.tap4food.user.app.security.model.User;
import com.endeavour.tap4food.user.app.security.model.UserDetailsImpl;
import com.endeavour.tap4food.user.app.security.model.UserRole;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private CommonRepository commonRepository;
	
	@Autowired
	private PasswordEncoder encoder;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		System.out.println("In loadUserByUsername()");
		
		System.out.println("OTP : " + otp);
		
		User user = null;
		
		if(Objects.isNull(otp)) {
			
		}else {
			System.out.println("Password from DB : " + otp.getOtp());
			
			String encodedPassword = encoder.encode(otp.getOtp());
			
			System.out.println("Encoded Password from DB : " + encodedPassword);
			
			user = new User(null, null, null, encodedPassword, otp.getPhoneNumber());
			
			user.setId(otp.getId());
			
			Set<UserRole> roles = new HashSet<UserRole>();
			
			UserRole userRole = new UserRole();
			userRole.setName(UserRoleEnum.CUSTOMER);
			
			roles.add(userRole);
			
			user.setRoles(roles);
		}

		return UserDetailsImpl.build(user);
	}

}
