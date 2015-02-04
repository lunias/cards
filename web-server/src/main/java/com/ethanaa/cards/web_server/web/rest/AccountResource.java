package com.ethanaa.cards.web_server.web.rest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.ethanaa.cards.common.web.rest.dto.RegistrationDTO;
import com.ethanaa.cards.common.web.rest.dto.UserDTO;
import com.ethanaa.cards.web_server.web.rest.interop.OAuthInterop;
import com.ethanaa.cards.web_server.web.rest.util.RestUtil;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);
    
    @Inject
    private OAuthInterop oauthServer;    
    
    /**
     * POST  /register -> register the user.
     */    
    @RequestMapping(value = "/register",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> registerAccount(@Valid @RequestBody UserDTO userDTO, HttpServletRequest request) {  
    	    	
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();     	
    	
        RegistrationDTO registrationDTO = new RegistrationDTO(userDTO, baseUrl);
        
    	ResponseEntity<?> response = oauthServer.exchange("/api/register", HttpMethod.POST, new HttpEntity<RegistrationDTO>(registrationDTO), String.class, request);    	
    	
    	if (RestUtil.isError(response.getStatusCode())) {
    		return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(response.getBody());
    	}   	
    	
    	return new ResponseEntity<>(response.getStatusCode());     	
    }
    
    /**
     * GET  /activate -> activate the registered user.
     */
    @RequestMapping(value = "/activate",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key, HttpServletRequest request) {
    	
    	ResponseEntity<String> response = oauthServer.exchange("/api/activate?key={key}", HttpMethod.GET, String.class, request, key);
    	
        return new ResponseEntity<String>(response.getBody(), response.getStatusCode());
    }

    /**
     * GET  /authenticate -> check if the user is authenticated, and return its login.
     */
    @RequestMapping(value = "/authenticate",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account -> get the current user.
     */
    @RequestMapping(value = "/account",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserDTO> getAccount(HttpServletRequest request) {    	
    	
    	ResponseEntity<UserDTO> response = oauthServer
    			.exchange("/api/account", HttpMethod.GET, UserDTO.class, request);    	    	
    	
    	return new ResponseEntity<UserDTO>(response.getBody(), response.getStatusCode());    	
    }       

    /**
     * POST  /account -> update the current user information.
     */
    @RequestMapping(value = "/account",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> saveAccount(@RequestBody UserDTO userDTO, HttpServletRequest request) {
    	
    	ResponseEntity<String> response = oauthServer.exchange("/api/account", HttpMethod.POST, new HttpEntity<UserDTO>(userDTO), String.class, request);
    	
    	return new ResponseEntity<String>(response.getBody(), response.getStatusCode());    	
    }

    /**
     * POST  /change_password -> changes the current user's password
     */
    @RequestMapping(value = "/account/change_password",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> changePassword(@RequestBody String password, HttpServletRequest request) {
    	
    	ResponseEntity<String> response = oauthServer.exchange("/api/account/change_password", HttpMethod.POST, new HttpEntity<String>(password), String.class, request);    	
    	
    	return new ResponseEntity<String>(response.getBody(), response.getStatusCode());    	
    }
}
