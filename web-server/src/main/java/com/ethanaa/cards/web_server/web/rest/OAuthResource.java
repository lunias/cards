package com.ethanaa.cards.web_server.web.rest;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ethanaa.cards.common.constant.ResourceConstants;
import com.ethanaa.cards.common.web.rest.interop.RestTemplateErrorHandler;
import com.ethanaa.cards.common.web.rest.util.RestUtil;

@RestController
@RequestMapping("/api/oauth")
public class OAuthResource implements EnvironmentAware {

	private static final String ENV_SERVERS = "servers.";
	private static final String PROP_OAUTH_HOST = "oauth.host";		
	
    private RelaxedPropertyResolver serverPropertyResolver;    
    
    private static final String CLIENT = ResourceConstants.WEB_RESOURCE.replaceFirst("cards-", "");    
    
    private static final Pattern USERNAME_PATT = Pattern.compile("username=(.*?)(&|$)");
    private static final Pattern PASSWORD_PATT = Pattern.compile("password=(.*?)(&|$)");    
    
	@RequestMapping(method = RequestMethod.POST, value = "/token")
	public ResponseEntity<?> redirectWithAuthorizationHeaders(
			@RequestBody String urlEncodedBody, HttpServletRequest request)
			throws Exception {				
		
		String oauthTokenResource = serverPropertyResolver.getProperty(PROP_OAUTH_HOST) + "/api/oauth/token";
		
		String username = "";
		Matcher usernameMatcher = USERNAME_PATT.matcher(urlEncodedBody);
		if (usernameMatcher.find()) {
			username = usernameMatcher.group(1);
		} else {
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
					.body(new InvalidClientException("Could not parse username"));
		}
		
		String password = "";
		Matcher passwordMatcher = PASSWORD_PATT.matcher(urlEncodedBody);
		if (passwordMatcher.find()) {
			password = passwordMatcher.group(1);
		} else {
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
					.body(new InvalidClientException("Could not parse password"));
		}		
		
		StringBuilder bodyBuilder = new StringBuilder("username=" + username);
		bodyBuilder.append("&password=" + password);
		bodyBuilder.append("&client_id=cards" + CLIENT);		
		bodyBuilder.append("&grant_type=password");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));	
		
		RestTemplate rest = new RestTemplate();
		rest.setErrorHandler(new RestTemplateErrorHandler());
		
		ResponseEntity<OAuth2AccessToken> accessTokenResponse = null;
		accessTokenResponse = rest.exchange(oauthTokenResource, HttpMethod.POST,
				new HttpEntity<String>(bodyBuilder.toString(), headers), OAuth2AccessToken.class);			
		
		if (RestUtil.isError(accessTokenResponse.getStatusCode())) {
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(accessTokenResponse.getBody());
		}
		
		return new ResponseEntity<>(accessTokenResponse.getBody(), accessTokenResponse.getStatusCode());		
	}
    
	@Override
	public void setEnvironment(Environment environment) {
		this.serverPropertyResolver = new RelaxedPropertyResolver(environment, ENV_SERVERS);
	}

}
