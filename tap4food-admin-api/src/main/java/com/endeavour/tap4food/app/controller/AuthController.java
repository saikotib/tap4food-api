package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.payload.request.LoginRequest;
import com.endeavour.tap4food.app.payload.response.JwtResponse;
import com.endeavour.tap4food.app.repository.UserRoleRepository;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.security.jwt.JwtUtils;
import com.endeavour.tap4food.app.security.model.UserDetailsImpl;
import com.endeavour.tap4food.app.service.AdminService;
import com.endeavour.tap4food.app.service.CommonService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/auth/admin")
@Api(tags = "AuthenticationController", description = "Authentication Controller for user loging & signup")
public class AuthController {
	
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRoleRepository roleRepository;

	@Autowired
	private PasswordEncoder encoder;
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private CommonService commonService;

	@Autowired
	JwtUtils jwtUtils;
	
	@RequestMapping(value = "/forgot-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> forgotPassword(@RequestParam("user-name") String userName) {

		Optional<Admin> adminUserData = adminService.findAdminUserByUserName(userName);
		
		ResponseHolder response = null;
		
		ResponseEntity<ResponseHolder> responseEntity = null;
		
		if(adminUserData.isPresent()) {
			Admin merchant = adminUserData.get();
			
			boolean smsSentFlag = commonService.sendOTPToPhone(merchant.getPhoneNumber());
			
			if(smsSentFlag){
				
				Map<String, String> dataMap = new HashMap<String, String>();
				
				dataMap.put("phoneNumber", merchant.getPhoneNumber());
				dataMap.put("message", "OTP has been delivered to customer registed phone number : " + merchant.getPhoneNumber());
				
				response = ResponseHolder.builder()
						.status("success")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data(dataMap)
						.build();
				
				responseEntity = ResponseEntity.ok().body(response);
			}else {
				response = ResponseHolder.builder()
						.status("error")
						.timestamp(String.valueOf(LocalDateTime.now()))
						.data("Problem occured while sending OTP to customer registed phone number : " + merchant.getPhoneNumber())
						.build();
				
				responseEntity = ResponseEntity.badRequest().body(response);
			}
			
			
		}else {
			
			response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("No admin user found with the given userName : " + userName)
					.build();
			
			responseEntity = ResponseEntity.badRequest().body(response);
		}
		
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/verify-otp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> verifyOtp(@RequestParam(value = "phone-number", required = true) String phoneNumber, 
			@RequestParam(value = "otp", required = true) String otp,
			@RequestParam(value = "forgot-password", required = false) boolean forgotPasswordFlag) {

		boolean isVerified = adminService.verifyOTP(phoneNumber, otp);
		
		ResponseHolder response = null;
		ResponseEntity<ResponseHolder> responseEntity = null;
		
		if(isVerified) {
			response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("OTP verified successfully!")
					.build();
			
			responseEntity = ResponseEntity.ok(response);
			
		}else {
			response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("Invalid OTP.")
					.build();
			
			responseEntity = ResponseEntity.badRequest().body(response);
		}
		
		return responseEntity;
	}
	

	@RequestMapping(value = "/signin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		
		System.out.println(loginRequest.getUsername());
		System.out.println(loginRequest.getPassword());
		
		System.out.println("admin :" + encoder.encode("admin"));
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(
				new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
	}

	@RequestMapping(value = "/create-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createPassword(@RequestParam("userName") String userName,
			@RequestParam("password") String password) {
		
		ResponseEntity<ResponseHolder> responseEntity = null;
		ResponseHolder response = null;
		
		if(adminService.createPassword(userName, encoder.encode(password))) {
			response  = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data("The password has been set succesfully..")
					.build();
			responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		} else {
			response  = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data("Error occured while setting the password")
					.build();
			responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

		

		return responseEntity;
	}
}
