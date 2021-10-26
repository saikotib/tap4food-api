package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

import com.endeavour.tap4food.app.enums.AccountStatusEnum;
import com.endeavour.tap4food.app.enums.UserRoleEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.payload.request.ChangePasswordRequest;
import com.endeavour.tap4food.app.payload.request.LoginRequest;
import com.endeavour.tap4food.app.payload.request.SignupRequest;
import com.endeavour.tap4food.app.payload.response.JwtResponse;
import com.endeavour.tap4food.app.repository.AuthenticationRoleRepository;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.security.jwt.JwtUtils;
import com.endeavour.tap4food.app.security.model.UserDetailsImpl;
import com.endeavour.tap4food.app.security.model.UserRole;
import com.endeavour.tap4food.app.service.CommonService;
import com.endeavour.tap4food.app.service.MerchantService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/auth/merchant")
@Api(tags = "AuthenticationController", description = "Authentication Controller for user loging & signup")
public class AuthController {
	
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private AuthenticationRoleRepository roleRepository;

	@Autowired
	private PasswordEncoder encoder;
	
	@Autowired
	private MerchantService merchantService;
	
	@Autowired
	private CommonService commonService;

	@Autowired
	JwtUtils jwtUtils;
	
	@RequestMapping(value = "/phone-number-login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> loginWithPhoneNumber(@RequestParam("phone-number") String phoneNumber) {

		boolean smsSentFlag = commonService.sendOTPToPhone(phoneNumber);
		ResponseHolder response = null;
		
		if(smsSentFlag){
			response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("OTP has been delivered to customer registed phone number : " + phoneNumber)
					.build();
		}else {
			response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("Problem occured while sending OTP to customer registed phone number : " + phoneNumber)
					.build();
		}
		
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/change-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) throws TFException {
		
		String message = merchantService.changePassword(changePasswordRequest.getUniqueNumber(), changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
		
		ResponseEntity<ResponseHolder> response = null;
		
		if(Objects.isNull(message)) {
			ResponseHolder responseHolder = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("No merchant found with the input unique number")
					.build();
			
			response = ResponseEntity.badRequest().body(responseHolder);
			
		}else {
			ResponseHolder responseHolder = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data(message)
					.build();
			
			response = ResponseEntity.badRequest().body(responseHolder);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/forgot-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> forgotPassword(@RequestParam("unique-number") Long uniqueNumber) {

		Optional<Merchant> merchantData = merchantService.findByUniqueNumber(uniqueNumber);
		
		ResponseHolder response = null;
		
		ResponseEntity<ResponseHolder> responseEntity = null;
		
		if(merchantData.isPresent()) {
			Merchant merchant = merchantData.get();
			
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
					.data("No merchant found with the given merchant unique number : " + uniqueNumber)
					.build();
			
			responseEntity = ResponseEntity.badRequest().body(response);
		}
		
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/verify-otp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> verifyOtp(@RequestParam(value = "phone-number", required = true) String phoneNumber, 
			@RequestParam(value = "otp", required = true) String otp,
			@RequestParam(value = "forgot-password", required = false) boolean forgotPasswordFlag) throws TFException {

		boolean isVerified = merchantService.verifyOTP(phoneNumber, otp, forgotPasswordFlag);
		
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
		
		System.out.println(loginRequest.getUniqueNumber());
		System.out.println(loginRequest.getPassword());
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUniqueNumber(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		
		if(userDetails.getStatus() == null) {
			System.out.println("Status is NULL. Need to check why it went null.");
			userDetails.setStatus(AccountStatusEnum.REQUEST_FOR_APPROVAL.name());
		}
		
		System.out.println("Status : " + userDetails.getStatus());
		
		ResponseEntity response = null;
		
		if (!Objects.isNull(userDetails)) {
			if (userDetails.getStatus().equalsIgnoreCase(AccountStatusEnum.INACTIVE.name())) {
				response = ResponseEntity.badRequest().body("Your account is not activated.");
			}else {
				List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
						.collect(Collectors.toList());
				
				if(Objects.nonNull(userDetails)) {
					response =ResponseEntity.ok(
							new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
				}else {
					response = ResponseEntity.badRequest().body("Error occured during merchant login");
				}
			}
		}
		
		return response;
	}
	
	@RequestMapping(value = "/resent-otp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> resendOTP(@RequestParam("phone-number") String phoneNumber){
		
		ResponseHolder responseHolder = null;
		
		ResponseEntity<ResponseHolder> responseEntity = null;
		
		boolean isOTPDelivered = merchantService.resentOtp(phoneNumber);
		
		if(isOTPDelivered) {
			responseHolder = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("OTP is sent again")
					.build();
			
			responseEntity = ResponseEntity.ok().body(responseHolder);
		}else {
			responseHolder = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("Couldn't send OTP.")
					.build();
			
			responseEntity = ResponseEntity.badRequest().body(responseHolder);
		}
		
		return responseEntity;
	}

	@RequestMapping(value = "/signup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> signup(@Valid @RequestBody SignupRequest signUpRequest) {
		
		final String email = signUpRequest.getEmail();
		final String phoneNumber = signUpRequest.getPhoneNumber();
		
		String dataValidation = merchantService.validateSignupData(email, phoneNumber);
		
		ResponseHolder response = null;
		
		ResponseEntity<ResponseHolder> responseEntity = null;
		
		if(Objects.nonNull(dataValidation)) {
			response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data(dataValidation)
					.build();
			
			responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
			
		}else{
			// Create new user's account
			Merchant merchant = new Merchant();
			merchant.setUserName(signUpRequest.getUsername());
			merchant.setEmail(signUpRequest.getEmail());
			merchant.setPhoneNumber(signUpRequest.getPhoneNumber());
			merchant.setStatus(AccountStatusEnum.REQUEST_FOR_APPROVAL.name());
			
//			User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPhoneNumber());

			Set<String> strRoles = signUpRequest.getRoles();
			
			Set<UserRole> roles = new HashSet<>();

			if (strRoles == null) {
				UserRole userRole = roleRepository.findByRole(UserRoleEnum.CUSTOMER)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				roles.add(userRole);
			} else {
				strRoles.forEach(role -> {
					
					System.out.println(role);
					switch (role) {
					case "ADMIN":
						UserRole adminRole = roleRepository.findByRole(UserRoleEnum.ADMIN)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(adminRole);

						break;
					case "MERCHANT":
						UserRole modRole = roleRepository.findByRole(UserRoleEnum.MERCHANT)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(modRole);

						break;
					default:
						UserRole userRole = roleRepository.findByRole(UserRoleEnum.CUSTOMER)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(userRole);
					}
				});
				
				responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
			}

			merchantService.saveMerchant(merchant);
			
			response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("Your details are saved. OTP has been delivered to mobile number.")
					.build();
			
			responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		}

		return responseEntity;
	}
	
	@RequestMapping(value = "/create-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createPassword(@RequestParam("uniqueNumber") Long uniqueNumber,
			@RequestParam("password") String password) {
		
		ResponseEntity<ResponseHolder> responseEntity = null;
		ResponseHolder response = null;
		
		if(merchantService.createPassword(uniqueNumber, encoder.encode(password))) {
			response  = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data("The password has been created succesfully..")
					.build();
			responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		
		} else {
			response  = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data("Error occured while creating password")
					.build();
			responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

		

		return responseEntity;
	}
}
