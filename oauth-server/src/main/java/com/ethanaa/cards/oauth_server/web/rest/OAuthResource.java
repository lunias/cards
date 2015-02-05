package com.ethanaa.cards.oauth_server.web.rest;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.postgresql.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/oauth")
public class OAuthResource implements EnvironmentAware {

    private static final String ENV_OAUTH = "authentication.clients.";
    private static final String PROP_CLIENTID = "clientid";	
	
	private final Logger log = LoggerFactory.getLogger(OAuthResource.class);

	@Inject
	private TokenStore tokenStore;
	
	@Inject
	private ConsumerTokenServices tokenServices;
	
	@Inject
	private ClientDetailsService clientDetailsService;	
    
    private RelaxedPropertyResolver propertyResolver;	
	
	@RequestMapping(method = RequestMethod.POST, value = "/token")
	public ResponseEntity<?> redirectWithAuthorizationHeaders(
			@RequestBody String body,
			HttpServletRequest request) throws Exception {	
		
		String[] queryParams = body.split("&");
		String clientIdParam = queryParams[3];
		String clientId = clientIdParam.substring(clientIdParam.indexOf('=') + 1);
		
		String client = clientId.replaceFirst("cards", "");
		String clientSecret = propertyResolver.getProperty(client + ".secret");
		
		URI forwardUri = new URI(request.getScheme(), 
				null, 
				request.getServerName(), 
				request.getServerPort(), 
				"/oauth/token",
				body, null);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBytes((clientId + ":" + clientSecret).getBytes()));
		
		RestTemplate rest = new RestTemplate();
		ResponseEntity<OAuth2AccessToken> accessTokenResponse
			= rest.exchange(forwardUri.toString(), HttpMethod.POST, new HttpEntity<String>(body, headers), OAuth2AccessToken.class); 
		
		return new ResponseEntity<>(accessTokenResponse.getBody(), accessTokenResponse.getStatusCode());
    }
    
	@RequestMapping(method = RequestMethod.GET, value = "/tokens")
	public ResponseEntity<Map<String, Object>> getActiveTokens() {
		
		String clientId = propertyResolver.getProperty(PROP_CLIENTID);
		
		ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);		
		Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId(clientId);	
		
		Map<String, Object> response = new HashMap<>();
		response.put("clientDetails", clientDetails);
		response.put("tokens", enhance(tokens));
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/clients/{client}/users/{username}/tokens")
	public ResponseEntity<Collection<OAuth2AccessToken>> getTokensForAccount(@PathVariable String client, @PathVariable String username, Principal principal) {
		
		checkResourceOwner(username, principal);		
		
		return new ResponseEntity<>(enhance(tokenStore.findTokensByClientIdAndUserName(client, username)), HttpStatus.OK);		
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/users/{username}/tokens/{token}")
	public ResponseEntity<Void> revokeToken(@PathVariable String username, @PathVariable String token, Principal principal) {
		
		checkResourceOwner(username, principal);
		
		if (tokenServices.revokeToken(token)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	private void checkResourceOwner(String username, Principal principal) {
		
		if (principal instanceof OAuth2Authentication) {
			
			OAuth2Authentication authentication = (OAuth2Authentication) principal;
			
			if (!authentication.isClientOnly() && !username.equals(principal.getName())) {
				throw new AccessDeniedException(String.format("User '%s' cannot obtain tokens for user '%s'",
						principal.getName(), username));
			}
		}
	}
	
	private Collection<OAuth2AccessToken> enhance(Collection<OAuth2AccessToken> tokens) {
		
		Collection<OAuth2AccessToken> result = new ArrayList<OAuth2AccessToken>();		
		
		for (OAuth2AccessToken prototype : tokens) {
			
			DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(prototype);
			OAuth2Authentication authentication = tokenStore.readAuthentication(token);
			
			if (authentication == null) {
				continue;
			}
			
			String clientId = authentication.getOAuth2Request().getClientId();
			if (clientId != null) {
				
				Map<String, Object> map = new HashMap<String, Object>(token.getAdditionalInformation());
				map.put("client_id", clientId);
				token.setAdditionalInformation(map);
				result.add(token);
			}
		}
		
		return result;
	}

	@Override
	public void setEnvironment(Environment environment) {
		propertyResolver = new RelaxedPropertyResolver(environment, ENV_OAUTH);
	}	
	
}
