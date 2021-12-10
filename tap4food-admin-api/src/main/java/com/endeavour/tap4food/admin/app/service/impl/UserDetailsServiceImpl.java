package com.endeavour.tap4food.admin.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.endeavour.tap4food.admin.app.repository.AdminRepository;
import com.endeavour.tap4food.admin.app.security.model.UserDetailsImpl;
import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.repository.CommonRepository;

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
		
		Admin admin = adminRepository.findAdminByUserName(userName)
				.orElseThrow(() -> new UsernameNotFoundException("Admin Not Found with username: " + userName));
		
		System.out.println("In loadUserByUsername() .. " + admin);

		return UserDetailsImpl.build(admin);
	}

}
