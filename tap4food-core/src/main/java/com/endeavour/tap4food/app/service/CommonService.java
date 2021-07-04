package com.endeavour.tap4food.app.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommonService {

	@Autowired
	private CommonRepository commonRepository;
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("${spring.mail.host}")
	private String mailHost;
	
	@Value("${spring.mail.port}")
	private int port;
	
	@Value("${spring.mail.username}")
	private String userName;
	
	@Value("${spring.mail.password}")
	private String password;
	
	@Bean
	public String getCreatePasswordHtmlContent() {
		
		String template = null;
		
		Resource resource = new ClassPathResource("emailTemplates/createPasswordEmail_Merchant.txt");
        try {
			InputStream inputStream = resource.getInputStream();
			
			byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
			template = new String(bdata, StandardCharsets.UTF_8);
            
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return template;
	}
	
	@Bean
	public String getResetPasswordHtmlContent() {
		
		String template = null;
		
		Resource resource = new ClassPathResource("emailTemplates/resetPasswordEmail_Merchant.txt");
        try {
			InputStream inputStream = resource.getInputStream();
			
			byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
			template = new String(bdata, StandardCharsets.UTF_8);
            
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return template;
	}
	
	public boolean sendSMS(String phoneNumber,String message) {
		String userId = "flyingkart";
		String password = "krish";
		String senderId = "TAPFOD";
		String partnerEntityId = "1201159245741531244";
		String templateId = "1207162433679098476";

		message = message.replaceAll("/", "%2F").replaceAll(":", "%3A").replaceAll("\\?", "%3F").replaceAll("=", "%3D").replaceAll("-", "%2D");
		
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
		return true;
	}

	public boolean sendOTPToPhone(final String phoneNumber) {
		
		String otp = CommonUtil.generateOTP();
		
		Otp otpObject = new Otp();
		otpObject.setIsExpired(false);
		otpObject.setOtp(otp);
		otpObject.setPhoneNumber(phoneNumber);
		
		commonRepository.persistOTP(otpObject);
		
		String message = String.format("%s is the OTP to login to your Tap4Food.please enter the OTP to verify your mobile number.", otp).replaceAll("\\s", "%20");

		ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
        emailExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sendSMS(phoneNumber, message);
            }
        });
        emailExecutor.shutdown();
		
		
		log.info("The OTP generated : {}", otp);
		
		return true;		
	}
	
	public Otp fetchOtp(final String phoneNumber) {
		
		boolean otpMatch = false;
		
		Otp otp = commonRepository.getRecentOtp(phoneNumber);
		
		return otp;		
	}
	
	public void sendEmail(final String reciepentMail, final String messageBody, final String subject) {
		
		MimeMessage message = javaMailSender.createMimeMessage();
		
		MimeMessageHelper helper = new MimeMessageHelper(message);

		try {
			helper.setTo(reciepentMail);
			helper.setSubject(subject);
			helper.setText(messageBody, true);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		javaMailSender.send(message);
			 
        
        log.info("Mail is delivered to : {}", reciepentMail);
	}
	
}
