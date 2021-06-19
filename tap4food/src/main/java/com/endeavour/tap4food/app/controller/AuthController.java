package com.endeavour.tap4food.app.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.enums.Role;
import com.endeavour.tap4food.app.payload.request.LoginRequest;
import com.endeavour.tap4food.app.payload.request.SignupRequest;
import com.endeavour.tap4food.app.payload.response.JwtResponse;
import com.endeavour.tap4food.app.payload.response.MessageResponse;
import com.endeavour.tap4food.app.repository.UserRepository;
import com.endeavour.tap4food.app.repository.UserRoleRepository;
import com.endeavour.tap4food.app.security.jwt.JwtUtils;
import com.endeavour.tap4food.app.security.model.User;
import com.endeavour.tap4food.app.security.model.UserDetailsImpl;
import com.endeavour.tap4food.app.security.model.UserRole;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserRoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@RequestMapping(value = "/signin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		
		System.out.println(loginRequest.getUsername());
		System.out.println(loginRequest.getPassword());

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

	@RequestMapping(value = "/signup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		/*
		 * if (userRepository.existsByUserName(signUpRequest.getUsername())) { return
		 * ResponseEntity.badRequest().body(new
		 * MessageResponse("Error: Username is already taken!")); }
		 * 
		 * if (userRepository.existsByEmail(signUpRequest.getEmail())) { return
		 * ResponseEntity.badRequest().body(new
		 * MessageResponse("Error: Email is already in use!")); }
		 */

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()), signUpRequest.getPhoneNumber());

		Set<String> strRoles = signUpRequest.getRoles();
		Set<UserRole> roles = new HashSet<>();

		if (strRoles == null) {
			UserRole userRole = roleRepository.findByRole(Role.CUSTOMER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				
				System.out.println(role);
				switch (role) {
				case "ADMIN":
					UserRole adminRole = roleRepository.findByRole(Role.ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "MERCHANT":
					UserRole modRole = roleRepository.findByRole(Role.MERCHANT)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				default:
					UserRole userRole = roleRepository.findByRole(Role.CUSTOMER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
}
