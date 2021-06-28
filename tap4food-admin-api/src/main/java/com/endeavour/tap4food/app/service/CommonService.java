package com.endeavour.tap4food.app.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommonService {

	@Autowired
	private CommonRepository commonRepository;

	public boolean sendOTPToPhone(final String phoneNumber) {
		
		String otp = CommonUtil.generateOTP();
		
		Otp otpObject = new Otp();
		otpObject.setIsExpired(false);
		otpObject.setOtp(otp);
		otpObject.setPhoneNumber(phoneNumber);
		
		commonRepository.persistOTP(otpObject);
		
		String userId = "flyingkart";
		String password = "krish";
		String senderId = "TAPFOD";
		String message = String.format("%s is the OTP to login to your Tap4Food.please enter the OTP to verify your mobile number.", otp).replaceAll("\\s", "%20");
		String partnerEntityId = "1201159245741531244";
		String templateId = "1207162433679098476";
		
		String url = String.format("http://tra.bulksmshyderabad.co.in/websms/sendsms.aspx?userid=%s&password=%s&sender=%s&mobileno=%s&msg=%s&peid=%s&tpid=%s", userId, password, senderId, phoneNumber, message, partnerEntityId, templateId);

		System.out.println("SMS URL : " + url);
		
		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
					
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			System.out.println("Response : " + response.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		log.info("The OTP generated : {}", otp);
		
		return true;		
	}
}
