package com.endeavour.tap4food.app.payload.request;

import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class SignupRequest {

	@NotBlank
    @Size(min = 2, max = 30)
    private String username;
 
    @NotBlank
    @Size(max = 60)
    @Email
    private String email;
    
    @NotBlank
    @Size(max = 20)
    private String phoneNumber;
    
    private Set<String> roles;
     
}
