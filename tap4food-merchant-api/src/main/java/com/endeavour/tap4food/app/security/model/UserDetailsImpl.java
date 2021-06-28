package com.endeavour.tap4food.app.security.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.endeavour.tap4food.app.enums.UserRoleEnum;
import com.endeavour.tap4food.app.model.Merchant;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/*
 * This file is used for common login & signup
 */

@Data
public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 1L;

	private String id;

	private String username;

	private String email;

	private String phoneNumber;
	
	private Long uniqueNumber;

	@JsonIgnore
	private String password;

	private Collection<? extends GrantedAuthority> authorities;

	public UserDetailsImpl(String id, String username, String email, String password, String phoneNumber, Long uniqueNumber,
			Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.uniqueNumber = uniqueNumber;
		this.authorities = authorities;
	}

	public static UserDetailsImpl build(Merchant merchant) {
		
		Set<UserRole> roles = new HashSet<>();
		UserRole userRole = new UserRole();
		userRole.setName(UserRoleEnum.MERCHANT);
		roles.add(userRole);

		List<GrantedAuthority> authorities = roles.stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name())).collect(Collectors.toList());

		return new UserDetailsImpl(merchant.getId(), merchant.getUserName(), merchant.getEmail(), merchant.getPassword(),
				merchant.getPhoneNumber(), merchant.getUniqueNumber(), authorities);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return authorities;
	}

	@Override
	public String getPassword() {

		return password;
	}

	@Override
	public String getUsername() {

		return username;
	}

	@Override
	public boolean isAccountNonExpired() {

		return true;
	}

	@Override
	public boolean isAccountNonLocked() {

		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserDetailsImpl user = (UserDetailsImpl) o;
		return Objects.equals(id, user.id);
	}
}
