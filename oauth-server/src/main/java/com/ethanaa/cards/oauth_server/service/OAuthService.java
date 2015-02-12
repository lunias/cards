package com.ethanaa.cards.oauth_server.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ethanaa.cards.common.web.rest.exception.OAuthClientDetailsNotFoundException;
import com.ethanaa.cards.oauth_server.domain.User;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthClientDetails;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthResource;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthScope;
import com.ethanaa.cards.oauth_server.repository.oauth.OAuthClientDetailsRepository;
import com.ethanaa.cards.oauth_server.repository.oauth.OAuthResourceRepository;
import com.ethanaa.cards.oauth_server.repository.oauth.OAuthScopeRepository;
import com.ethanaa.cards.oauth_server.security.SecurityUtils;
import com.ethanaa.cards.oauth_server.web.rest.oauth.OAuthClientDetailsResource;

@Service
@Transactional
public class OAuthService {

	@Inject
	private OAuthClientDetailsRepository oAuthClientDetailsRepository;
	
	@Inject
	private OAuthResourceRepository oAuthResourceRepository;
	
	@Inject
	private OAuthScopeRepository oAuthScopeRepository;
	
	@Inject
	private AuditEventRepository auditEventRepository;
	
    @Transactional(readOnly = true)
	public List<OAuthResource> getOAuthResources() {
		
		return oAuthResourceRepository.findAll();
	}
    
    @Transactional(readOnly = true)
    public List<OAuthScope> getOAuthScopes() {
    	
    	return oAuthScopeRepository.findAll();
    }
    
    public SimpleEntry<OAuthClientDetails, Boolean> createOrUpdateOAuthClientDetails(OAuthClientDetailsResource resource) {
    	
		OAuthClientDetails clientDetails = oAuthClientDetailsRepository.findOne(resource.getClientId());
		
		boolean created = clientDetails == null;
    	
    	OAuthClientDetails updatedClientDetails = new OAuthClientDetails(resource);
    	
    	return new SimpleEntry<>(oAuthClientDetailsRepository.save(updatedClientDetails), created);
    }
	
	@SuppressWarnings("serial")
	public void deleteOAuthClientDetails(String clientId) throws OAuthClientDetailsNotFoundException {
		
		final OAuthClientDetails clientDetails = oAuthClientDetailsRepository.findOne(clientId);
		
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
		
		auditEventRepository.add(new AuditEvent(SecurityUtils.getCurrentLogin(), "DELETE OAuthClientDetails", new HashMap<String, Object>() {
			{
				put("message", clientDetails);
			}
		}));
	}		
	
}