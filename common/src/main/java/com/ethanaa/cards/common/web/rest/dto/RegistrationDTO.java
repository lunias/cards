package com.ethanaa.cards.common.web.rest.dto;

import javax.validation.constraints.NotNull;

public class RegistrationDTO {

	@NotNull
	private UserDTO userDTO;
	
	@NotNull
	private String baseUrl;
	
	public RegistrationDTO() {		
	}
	
	public RegistrationDTO(UserDTO userDTO, String baseUrl) {
		
		this.userDTO = userDTO;
		this.baseUrl = baseUrl;
	}

	public UserDTO getUserDTO() {
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
