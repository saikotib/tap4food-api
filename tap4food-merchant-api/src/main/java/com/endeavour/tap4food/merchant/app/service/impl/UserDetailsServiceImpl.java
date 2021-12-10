package com.endeavour.tap4food.merchant.app.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.endeavour.tap4food.app.enums.AccountStatusEnum;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.merchant.app.security.model.UserDetailsImpl;
import com.endeavour.tap4food.merchant.app.service.MerchantService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private MerchantService merchantService;
	
	@Autowired
	private PasswordEncoder encoder;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String uniqueNumber) throws UsernameNotFoundException {
		
		System.out.println("In loadUserByUsername() .. START : " + uniqueNumber);
		
		Merchant merchant = merchantService.findByUniqueNumber(Long.valueOf(uniqueNumber))
				.orElseThrow(() -> new UsernameNotFoundException("Merchant Not Found with username: " + uniqueNumber));
		
		System.out.println("In loadUserByUsername() .. " + merchant);

		return UserDetailsImpl.build(merchant);
	}

}
