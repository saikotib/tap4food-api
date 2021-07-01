package com.endeavour.tap4food.app.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.repository.AdminRepository;
import com.endeavour.tap4food.app.util.EmailTemplateConstants;
import com.endeavour.tap4food.app.model.Merchant;
@Service
public class AdminService {

	
	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private CommonService commonService;
	
	public boolean createMerchant(Merchant merchant) {
		boolean flag = false;
		flag = adminRepository.createMerchant(merchant);
		
		Optional<Merchant> merchantOptionalObject = adminRepository.findByMerchantByPhoneNumber(merchant.getPhoneNumber());
		
		if(merchantOptionalObject.isPresent()) {
			
			merchant = merchantOptionalObject.get();
			
			if(Objects.isNull(merchant.getUniqueNumber())) {
				
				Long uniqNumber = this.getUniqueNumber();
				
				merchant.setUniqueNumber(uniqNumber);
				
				adminRepository.updateUniqueNumber(merchant);
				
				System.out.println("Unique number is updated forthe merchant...");
				
				String merchantEmail = merchant.getEmail();
				
				String createPasswordLink = "https://qa.d2sid2ekjjxq24.amplifyapp.com/merchant/createPassword?uniqueNumber=" + uniqNumber;
				
				String message = commonService.getCreatePasswordHtmlContent().replaceAll(EmailTemplateConstants.CREATE_NEW_PASSWORD_LINK, createPasswordLink)
						.replaceAll(EmailTemplateConstants.UNIQUE_NUMBER, String.valueOf(uniqNumber));
				
				String subject = "Tap4Food registration successfull";
				
				commonService.sendEmail(merchantEmail, message, subject);
			}
		}

		return flag;
	}
	
	private Long getUniqueNumber() {

		Long uniqNumber = adminRepository.getRecentUniqueNumber();

		return uniqNumber;
	}
}
