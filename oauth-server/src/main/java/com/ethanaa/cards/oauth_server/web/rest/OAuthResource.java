package com.ethanaa.cards.oauth_server.web.rest;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ethanaa.cards.common.constant.AuthorityConstants;
import com.ethanaa.cards.common.constant.ScopeConstants;
import com.ethanaa.cards.common.web.rest.exception.OAuthClientDetailsNotFoundException;
import com.ethanaa.cards.common.web.rest.interop.RestTemplateErrorHandler;
import com.ethanaa.cards.common.web.rest.util.RestUtil;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthClientDetails;
import com.ethanaa.cards.oauth_server.repository.oauth.OAuthClientDetailsRepository;
import com.ethanaa.cards.oauth_server.security.CustomJdbcClientDetailsService;
import com.ethanaa.cards.oauth_server.service.UserService;

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
	private CustomJdbcClientDetailsService clientDetailsService;
	
	@Inject
	private UserService userService;
	
	@Inject
	OAuthClientDetailsRepository oAuthClientDetailsRepository;
    
    private RelaxedPropertyResolver propertyResolver;	
	
    private static final Pattern CLIENT_ID_PATT = Pattern.compile("client_id=(.*?)(&|$)");
    private static final Pattern USERNAME_PATT = Pattern.compile("username=(.*?)(&|$)");   
    
    /**
     * 
     * 
     * @param urlEncodedBody
     * @param request
     * @return {@link OAuth2AccessToken} oauth2 authorization token
     * @throws Exception
     */
	@RequestMapping(method = RequestMethod.POST, value = "/token")
	public ResponseEntity<?> redirectWithAuthorizationHeaders(
			@RequestBody String urlEncodedBody,
			HttpServletRequest request) throws Exception {	
		
		String client = "cards-oauth";
		
		Matcher clientIdMatcher = CLIENT_ID_PATT.matcher(urlEncodedBody);
		if (clientIdMatcher.find()) {
			client = clientIdMatcher.group(1);
		} else {
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
					.body(new InvalidClientException("Could not parse client id"));
		}
		
		String username = "";
		Matcher usernameMatcher = USERNAME_PATT.matcher(urlEncodedBody);
		if (usernameMatcher.find()) {
			username = usernameMatcher.group(1);
		} else {
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
					.body(new InvalidClientException("Could not parse username"));
		}
		
		OAuthClientDetails requestedClientDetails = null;
		for(OAuthClientDetails clientDetails : userService.getUserClientDetails(username)) {
			if (clientDetails.getClientId().equals(client)) {
				requestedClientDetails = clientDetails;
				break;
			}
		}
		
		if (requestedClientDetails == null) {
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
					.body(new InvalidClientException("User '" + username + "' is not authorized for client '" + client + "'"));				
		}		
		
		String clientSecret = requestedClientDetails.getClientSecret();
		
		URI tokenUri = null;
		try {
			tokenUri = new URI(request.getScheme(), 
					null, 
					request.getServerName(), 
					request.getServerPort(), 
					"/oauth/token",
					urlEncodedBody, null);
		} catch (Exception e) {
			return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Could not parse request body");				
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.encodeBytes((client + ":" + clientSecret).getBytes()));
		
		RestTemplate rest = new RestTemplate();
		rest.setErrorHandler(new RestTemplateErrorHandler());
		
		ResponseEntity<OAuth2AccessToken> accessTokenResponse = null;
		accessTokenResponse = rest.exchange(tokenUri.toString(), HttpMethod.POST,
				new HttpEntity<String>(urlEncodedBody, headers), OAuth2AccessToken.class);			
		
		if (RestUtil.isError(accessTokenResponse.getStatusCode())) {
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(accessTokenResponse.getBody());
		}
		
		return new ResponseEntity<>(accessTokenResponse.getBody(), accessTokenResponse.getStatusCode());
    }
    
	/**
	 * GET all {@link OAuthClientDetails} records
	 * 
	 * @return a {@link List} of  {@link OAuthClientDetails} 
	 */
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")    	
	@RequestMapping(method = RequestMethod.GET, value = "/clients")
	public ResponseEntity<List<OAuthClientDetails>> getOAuthClients() {
		
		return new ResponseEntity<>(oAuthClientDetailsRepository.findAll(), HttpStatus.OK);
	}
	
	/**
	 * GET a specific {@link OAuthClientDetails} record
	 * 
	 * @param clientId
	 * @return {@link OAuthClientDetails}
	 */
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")      
	@RequestMapping(method = RequestMethod.GET, value = "/clients/{clientId}")
	public ResponseEntity<OAuthClientDetails> getOAuthClient(@PathVariable String clientId) {
		
		OAuthClientDetails clientDetails = oAuthClientDetailsRepository.findOne(clientId);
		if (clientDetails == null) {
			throw new OAuthClientDetailsNotFoundException(clientId);
		}
		
		return new ResponseEntity<>(clientDetails, HttpStatus.OK);
	}
    
	/**
	 * PUT a specific {@link OAuthClientDetails} record
	 * 
	 * @param clientId
	 * @param request
	 * @return {@link OAuthClientDetails}
	 */
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_WRITE + "')")      
	@RequestMapping(method = RequestMethod.PUT, value = "/clients/{clientId}")    
    public ResponseEntity<OAuthClientDetails> updateOAuthClient(@PathVariable String clientId, @RequestBody ClientDetails request) {    	    	
    	
		OAuthClientDetails clientDetails = oAuthClientDetailsRepository.findOne(clientId);
		if (clientDetails == null) {
			throw new OAuthClientDetailsNotFoundException(clientId);
		}
		
//		if (request.getResourceIds() != null) clientDetails.setResourceIds(request.getResourceIds());
//		if (request.getScope() != null) clientDetails.setScope(request.getScope());
//		if (request.getAccessTokenValidity() != null) clientDetails.setAccessTokenValidity(request.getAccessTokenValidity());
//		if (request.getAutoApprove() != null) clientDetails.setAutoApprove(request.getAutoApprove());
		
		return new ResponseEntity<>(oAuthClientDetailsRepository.save(clientDetails), HttpStatus.OK);
    }
    
	/**
	 * DELETE a specific {@link OAuthClientDetails} record
	 * 
	 * @param clientId
	 * @return {@link OAuthClientDetails}
	 */
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_DELETE + "')")      
	@RequestMapping(method = RequestMethod.DELETE, value = "/clients/{clientId}")    
    public ResponseEntity<?> deleteOAuthClient(@PathVariable String clientId) {    	
    	
		OAuthClientDetails clientDetails = oAuthClientDetailsRepository.findOne(clientId);
		if (clientDetails == null) {
			throw new OAuthClientDetailsNotFoundException(clientId);
		}		
		
		oAuthClientDetailsRepository.delete(clientDetails);
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }    
	
	/**
	 * 
	 * @return
	 */
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
	
	/**
	 * 
	 * @param client
	 * @param username
	 * @param principal
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/clients/{client}/users/{username}/tokens")
	public ResponseEntity<Collection<OAuth2AccessToken>> getTokensForAccount(@PathVariable String client, @PathVariable String username, Principal principal) {
		
		checkResourceOwner(username, principal);		
		
		return new ResponseEntity<>(enhance(tokenStore.findTokensByClientIdAndUserName(client, username)), HttpStatus.OK);		
	}
	
	/**
	 * 
	 * @param username
	 * @param token
	 * @param principal
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/users/{username}/tokens/{token}")
	public ResponseEntity<Void> revokeToken(@PathVariable String username, @PathVariable String token, Principal principal) {
		
		checkResourceOwner(username, principal);
		
		if (tokenServices.revokeToken(token)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * 
	 * @param username
	 * @param principal
	 */
	private void checkResourceOwner(String username, Principal principal) {
		
		if (principal instanceof OAuth2Authentication) {
			
			OAuth2Authentication authentication = (OAuth2Authentication) principal;
			
			if (!authentication.isClientOnly() && !username.equals(principal.getName())) {
				throw new AccessDeniedException(String.format("User '%s' cannot obtain tokens for user '%s'",
						principal.getName(), username));
			}
		}
	}
	
	/**
	 * 
	 * @param tokens
	 * @return
	 */
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
