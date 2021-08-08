package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.enums.UserRoleEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.payload.request.LoginRequest;
import com.endeavour.tap4food.app.payload.request.SignupRequest;
import com.endeavour.tap4food.app.payload.response.JwtResponse;
import com.endeavour.tap4food.app.repository.UserRoleRepository;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.security.jwt.JwtUtils;
import com.endeavour.tap4food.app.security.model.User;
import com.endeavour.tap4food.app.security.model.UserDetailsImpl;
import com.endeavour.tap4food.app.security.model.UserRole;
import com.endeavour.tap4food.app.service.CustomerService;
import com.endeavour.tap4food.app.service.UserService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/auth/user")
@Api(tags = "AuthenticationController", description = "Authentication Controller for user loging & signup")
public class AuthController {
	
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRoleRepository roleRepository;

	@Autowired
	private CustomerService customerService;

	@Autowired
	JwtUtils jwtUtils;
	
	@RequestMapping(value = "/phone-number-login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> loginWithPhoneNumber(@RequestParam("phone-number") String phoneNumber) throws TFException {

		boolean smsSentFlag = customerService.sendOTPToPhone(phoneNumber);
		ResponseHolder response = null;
		
		if(smsSentFlag){
			response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("OTP has been delivered to customer registed phone number : " + phoneNumber)
					.build();
		}else {
			throw new TFException("Problem occured while sending OTP to customer registed phone number : " + phoneNumber);
		}
		
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/verify-otp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> verifyOtp(@RequestParam("phone-number") String phoneNumber, @RequestParam("otp") String otp) throws TFException {

		boolean isVerified = customerService.verifyOTP(phoneNumber, otp);
		
		ResponseHolder response = null;
		if(isVerified) {
			response = ResponseHolder.builder()
					.status("success")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("OTP verified successfully!")
					.build();
		}else {
			response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("Invalid OTP.")
					.build();
		}
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/update-otp-expiry", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateOtpExpiryStatus(@RequestParam("phone-number") String phoneNumber) throws TFException {

		customerService.setOtpExpited(phoneNumber);
		
		ResponseHolder response = null;
		
		response = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data("OTP expiry is set successfully!")
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	

	@RequestMapping(value = "/signin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws TFException {
		
		System.out.println(loginRequest.getPhoneNumber());
		System.out.println(loginRequest.getOtp());
		
		customerService.verifyOTP(loginRequest.getPhoneNumber(), loginRequest.getOtp());
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getPhoneNumber(), loginRequest.getOtp()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(
				new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
	}

	@RequestMapping(value = "/signup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		
		final String email = signUpRequest.getEmail();
		final String phoneNumber = signUpRequest.getPhoneNumber();
		
		String dataValidation = userService.validateSignupData(email, phoneNumber);
		
		ResponseHolder response = null;
		
		if(Objects.nonNull(dataValidation)) {
			response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data(dataValidation)
					.build();
		}else{
			// Create new user's account
			User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPhoneNumber());

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
			}

			user.setRoles(roles);
			
			userService.saveUser(user);
			
			response = ResponseHolder.builder()
					.status("error")
					.timestamp(String.valueOf(LocalDateTime.now()))
					.data("Your details are saved. OTP has been delivered to mobile number.")
					.build();
		}

		return ResponseEntity.ok(response);
	};
	
	
	@RequestMapping(value = "/resent-otp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> resendOTP(@RequestParam("phone-number") String phoneNumber) throws TFException{
		
		userService.resendOtp(phoneNumber);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.data("OTP is sent again")
				.build();
		
		ResponseEntity<ResponseHolder> responseEntity = ResponseEntity.ok().body(responseHolder);
		
		
		return responseEntity;
	}

}
