package com.endeavour.tap4food.app.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.endeavour.tap4food.app.model.Otp;
import com.endeavour.tap4food.app.repository.CommonRepository;
import com.endeavour.tap4food.app.util.CommonUtil;
import com.endeavour.tap4food.app.util.MediaConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommonService {
	
	private static final Logger logger = Logger.getLogger(CommonService.class.getName());

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
	
	@Value("${images.base.path}")
	private String mediaBaseLocation;
	
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
		otpObject.setOtpSentTimeInMs(System.currentTimeMillis());
		
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
	
	public void createMediaFolderStructure(Long merchantNumber) {
		
		File merchantProfilePicDir = new File(getMerhantMediaDirs().get(MediaConstants.GET_KEY_MERCHANT_PROFILE_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(merchantNumber)));
		File merchantPesonalIdPicDir = new File(getMerhantMediaDirs().get(MediaConstants.GET_KEY_MERCHANT_PERSONAL_ID_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(merchantNumber)));
		
		if(!merchantProfilePicDir.exists()) {
			merchantProfilePicDir.mkdirs();
			logger.info("The merchant profile pics directory is created");
		}
		
		logger.info("merchantPesonalIdPicDir : " + merchantPesonalIdPicDir.getAbsolutePath());
		
		if(!merchantPesonalIdPicDir.exists()) {
			merchantPesonalIdPicDir.mkdirs();
			logger.info("The merchant personal ID pics directory is created");
		}
		
		logger.info("merchantPesonalIdPicDir : " + merchantPesonalIdPicDir.getAbsolutePath());
	}
	
	public void createMediaFolderStructure(Long merchantNumber, Long foodStallId) {
		
		File stallMenuPicsDir = new File(getMerhantMediaDirs().get(MediaConstants.GET_KEY_MENU_PIC_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(merchantNumber)).replaceAll(MediaConstants.IDENTIFIER_FSID, String.valueOf(foodStallId)));
		File stallProfilePicDic = new File(getMerhantMediaDirs().get(MediaConstants.GET_KEY_STALL_PROFILE_PIC_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(merchantNumber)).replaceAll(MediaConstants.IDENTIFIER_FSID, String.valueOf(foodStallId)));
		File foodItemPicDir = new File(getMerhantMediaDirs().get(MediaConstants.GET_KEY_FOODITEM_PIC_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(merchantNumber)).replaceAll(MediaConstants.IDENTIFIER_FSID, String.valueOf(foodStallId)));
		File offerPicDir = new File(getMerhantMediaDirs().get(MediaConstants.GET_KEY_OFFER_PIC_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(merchantNumber)).replaceAll(MediaConstants.IDENTIFIER_FSID, String.valueOf(foodStallId)));
		
		createMediaFolderStructure(merchantNumber);
		
		if(!stallMenuPicsDir.exists()) {
			stallMenuPicsDir.mkdirs();
			logger.info("The Foodstall menu pics directory is created");
		}
		
		if(!stallProfilePicDic.exists()) {
			stallProfilePicDic.mkdirs();
			logger.info("The Foodstall profile pics directory is created");
		}
		
		if(!foodItemPicDir.exists()) {
			foodItemPicDir.mkdirs();
			logger.info("The Fooditem pics directory is created");
		}
		
		if(!offerPicDir.exists()) {
			offerPicDir.mkdirs();
			logger.info("The Offer pics directory is created");
		}
		
		logger.info("stallMenuPicsDir : " + stallMenuPicsDir.getAbsolutePath());
		
		logger.info("stallProfilePicDic : " + stallProfilePicDic.getAbsolutePath());
		
		logger.info("foodItemPicDir : " + foodItemPicDir.getAbsolutePath());
		
		logger.info("offerPicDir : " + offerPicDir.getAbsolutePath());
	}

	@Bean
	public Map<String, String> getMerhantMediaDirs() {
		Map<String, String> dirsMap = new HashMap<String, String>();
		dirsMap.put(MediaConstants.GET_KEY_MERCHANT_PROFILE_DIR, mediaBaseLocation + File.separator + MediaConstants.IDENTIFIER_MERCHANTID + File.separator + MediaConstants.MERCHANT_PROFILE_PIC);
		dirsMap.put(MediaConstants.GET_KEY_MERCHANT_PERSONAL_ID_DIR, mediaBaseLocation + File.separator + MediaConstants.IDENTIFIER_MERCHANTID + File.separator + MediaConstants.MERCHANT_PESRONALID_PIC);
		dirsMap.put(MediaConstants.GET_KEY_STALL_PROFILE_PIC_DIR, mediaBaseLocation + File.separator + MediaConstants.IDENTIFIER_MERCHANTID + File.separator + "Stalls" + File.separator + MediaConstants.IDENTIFIER_FSID + File.separator + MediaConstants.FOODSTALL_PROFILE_PIC);
		dirsMap.put(MediaConstants.GET_KEY_MENU_PIC_DIR, mediaBaseLocation + File.separator + MediaConstants.IDENTIFIER_MERCHANTID + File.separator + "Stalls" + File.separator + MediaConstants.IDENTIFIER_FSID + File.separator + MediaConstants.FOODSTALL_MENU_PICS);
		dirsMap.put(MediaConstants.GET_KEY_FOODITEM_PIC_DIR, mediaBaseLocation + File.separator + MediaConstants.IDENTIFIER_MERCHANTID + File.separator + "Stalls" + File.separator + MediaConstants.IDENTIFIER_FSID + File.separator + MediaConstants.FOOD_ITEM_PIC);
		dirsMap.put(MediaConstants.GET_KEY_OFFER_PIC_DIR, mediaBaseLocation + File.separator + MediaConstants.IDENTIFIER_MERCHANTID + File.separator + "Stalls" + File.separator + MediaConstants.IDENTIFIER_FSID + File.separator + MediaConstants.OFFERS);
	
		System.out.println(dirsMap);
		return dirsMap;
	}
	
	@Bean
	public Map<String, String> getAdminMediaDirs() {
		Map<String, String> dirsMap = new HashMap<String, String>();
		dirsMap.put(MediaConstants.GET_KEY_BUSINESS_UNITS_DIR_ADMIN, mediaBaseLocation + File.separator + MediaConstants.ADMIN_BUSINESS_UNITS + File.separator + MediaConstants.IDENTIFIER_BUID);
		dirsMap.put(MediaConstants.GET_KEY_FOODCOURTS_DIR_ADMIN, mediaBaseLocation + File.separator + MediaConstants.ADMIN_FOODCOURTS + File.separator + MediaConstants.IDENTIFIER_FCID);
	
		System.out.println(dirsMap);
		return dirsMap;
	}
	
	public String getMediaBaseLocation() {
		return this.mediaBaseLocation;
	}
	
	public Long getTimeDiff(Long otpSentTimeInMs) {
		
		Long currentTimeInMs = System.currentTimeMillis();
		
		Long diff = currentTimeInMs - otpSentTimeInMs;
		
		return diff;
	}
}
