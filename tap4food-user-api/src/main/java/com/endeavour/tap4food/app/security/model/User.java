package com.endeavour.tap4food.app.security.model;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

	@Id
	private String id;
	
	private String fullName;
	
	@Size(max = 50)
	private String email;
	
	private String phoneNumber;
	
	@Size(max = 50)
	private String userName;
	
	@Size(max = 50)
	private String otp;
	
	@JsonIgnore
	private String password;
	
	private String status;
	
	@DBRef
	private Set<UserRole> roles = new HashSet<>();
	
	public User(String fullName, String username, String email, String otp, String phoneNumber) {
	    this.fullName = fullName;
		this.userName = username;
	    this.email = email;
	    this.otp = otp;
	    this.phoneNumber = phoneNumber;
	}
	
	public User(String username, String email, String phoneNumber) {
	    this.userName = username;
	    this.email = email;
	    this.phoneNumber = phoneNumber;
	}
}
