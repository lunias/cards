package com.ethanaa.cards.oauth_server.service.util;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.ethanaa.cards.common.web.rest.exception.OAuthClientDetailsNotFoundException;
import com.ethanaa.cards.oauth_server.domain.User;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthClientDetails;
import com.ethanaa.cards.oauth_server.repository.oauth.OAuthClientDetailsRepository;

@Service
@Transactional
public class OAuthService {

	@Inject
	private OAuthClientDetailsRepository oAuthClientDetailsRepository;
	
	public void deleteOAuthClientDetails(String clientId) throws OAuthClientDetailsNotFoundException {
		
		OAuthClientDetails clientDetails = oAuthClientDetailsRepository.findOne(clientId);
		
		if (clientDetails == null) {
			throw new OAuthClientDetailsNotFoundException(clientId);
		}
		
		clientDetails.getResources().clear();
		clientDetails.getScopes().clear();
		clientDetails.getAuthorities().clear();
		for(User user : clientDetails.getUsersThatCanAccess()) {
			user.getClientDetails().remove(clientDetails);
		}
		
		oAuthClientDetailsRepository.delete(clientDetails);
	}
	
}
