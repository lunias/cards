package com.ethanaa.cards.common.web.rest.resource;

import javax.validation.constraints.NotNull;

public class RegistrationDTO {

	@NotNull
	private UserResource userDTO;
	
	@NotNull
	private String baseUrl;
	
	public RegistrationDTO() {		
	}
	
	public RegistrationDTO(UserResource userDTO, String baseUrl) {
		
		this.userDTO = userDTO;
		this.baseUrl = baseUrl;
	}

	public UserResource getUserDTO() {
		return userDTO;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	@Override
	public String toString() {
		return "RegistrationDTO [userDTO=" + userDTO + ", baseUrl=" + baseUrl
				+ "]";
	}		
	
}
