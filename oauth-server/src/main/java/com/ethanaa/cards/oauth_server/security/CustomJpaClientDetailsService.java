package com.ethanaa.cards.oauth_server.security;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import com.ethanaa.cards.oauth_server.domain.oauth.OAuthClientDetails;
import com.ethanaa.cards.oauth_server.repository.oauth.OAuthClientDetailsRepository;

public class CustomJpaClientDetailsService implements ClientDetailsService, ClientRegistrationService {

	@Inject
	private OAuthClientDetailsRepository clientDetailsRepository;		
	
	@Override
	public void addClientDetails(ClientDetails clientDetails)
			throws ClientAlreadyExistsException {

		if (clientDetailsRepository.findOne(clientDetails.getClientId()) == null) {
			throw new ClientAlreadyExistsException("Client already exists: " + clientDetails.getClientId());
		}		
			
		clientDetailsRepository.save(new OAuthClientDetails(clientDetails));				
	}

	@Override
	public void updateClientDetails(ClientDetails clientDetails)
			throws NoSuchClientException {

		if (clientDetailsRepository.findOne(clientDetails.getClientId()) == null) {
			throw new NoSuchClientException("No client found with id = " + clientDetails.getClientId());
		}		
		
		clientDetailsRepository.save(new OAuthClientDetails(clientDetails));
		
	}

	@Override
	public void updateClientSecret(String clientId, String secret)
			throws NoSuchClientException {

		OAuthClientDetails clientDetails = clientDetailsRepository.findOne(clientId);
		if (clientDetails == null) {
			throw new NoSuchClientException("No client found with id = " + clientId);
		}
		
		// TODO encrypt secrets
		clientDetails.setClientSecret(secret);
		
		clientDetailsRepository.save(clientDetails);		
	}

	@Override
	public void removeClientDetails(String clientId)
			throws NoSuchClientException {

		OAuthClientDetails clientDetails = clientDetailsRepository.findOne(clientId);
		if (clientDetails == null) {
			throw new NoSuchClientException("No client found with id = " + clientId);
		}
		
		clientDetailsRepository.delete(clientDetails);
	}

	@Override
	public List<ClientDetails> listClientDetails() {		
		
		return new ArrayList<ClientDetails>(clientDetailsRepository.findAll());
	}

	@Override
	public ClientDetails loadClientByClientId(String clientId)
			throws ClientRegistrationException {

		OAuthClientDetails clientDetails = clientDetailsRepository.findOne(clientId);
		if (clientDetails == null) {
			throw new NoSuchClientException("No client with requested id: " + clientId);
		}		
		
		return clientDetails;
	}

}
