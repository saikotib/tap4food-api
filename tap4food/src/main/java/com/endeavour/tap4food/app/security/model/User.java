package com.endeavour.tap4food.app.security.model;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

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
	
	@NotBlank
	@Size(max = 50)
	private String email;
	
	private String phoneNumber;
	
	@NotBlank
	@Size(max = 50)
	private String userName;
	
	@NotBlank
	@Size(max = 50)
	private String password;
	
	@DBRef
	private Set<UserRole> roles = new HashSet<>();
	
	public User(String username, String email, String password, String phoneNumber) {
	    this.userName = username;
	    this.email = email;
	    this.password = password;
	    this.phoneNumber = phoneNumber;
	}
}
