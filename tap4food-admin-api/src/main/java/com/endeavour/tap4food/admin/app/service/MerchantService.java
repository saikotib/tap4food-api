package com.endeavour.tap4food.admin.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.endeavour.tap4food.admin.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.service.CommonService;

@Service
public class MerchantService {

	@Autowired
	private CommonRepository commonRepository;

	@Autowired
	private CommonService commonService;

	@Autowired
	PasswordEncoder encoder;
	
	private static final String MERCHANT_API = "http://localhost:8080/api/merchant"; 
	
	
	public ResponseHolder getMerchantInfo(Long merchantNumber) {
		
		RestTemplate restTemplate = new RestTemplate();
		
		String apiUrl = MERCHANT_API + "/get-merchant-details?uniqueNumber=" + merchantNumber;
		
	    ResponseHolder response = restTemplate.getForObject(apiUrl, ResponseHolder.class);
	    
	    System.out.println("In admin merchant api : " + response);
	    
	    return response;
	}
	
}
