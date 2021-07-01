package com.endeavour.tap4food.app.service.impl;

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

import com.endeavour.tap4food.app.enums.UserRoleEnum;
import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.AdminRepository;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.security.model.User;
import com.endeavour.tap4food.app.security.model.UserDetailsImpl;
import com.endeavour.tap4food.app.security.model.UserRole;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private CommonRepository commonRepository;
	
	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private PasswordEncoder encoder;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		
		System.out.println("In loadUserByUsername() .. START : " + userName);
		
		Admin admin = adminRepository.findByUserName(userName)
				.orElseThrow(() -> new UsernameNotFoundException("Admin Not Found with username: " + userName));
		
		System.out.println("In loadUserByUsername() .. " + admin);

		return UserDetailsImpl.build(admin);
	}

}
