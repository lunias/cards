package com.ethanaa.cards.web_server.web.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.ethanaa.cards.common.constant.AuthoritiesConstants;
import com.ethanaa.cards.common.domain.User;
import com.ethanaa.cards.web_server.web.rest.interop.OAuthInterop;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class UserResource {

	@Inject
	private OAuthInterop oauthServer;

    /**
     * GET  /users/:login -> get the "login" user.
     */
    @RequestMapping(value = "/users/{login}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> getUser(@PathVariable String login, HttpServletRequest request) {
    	
    	ResponseEntity<User> response = oauthServer
    			.exchange("/api/users/{login}", HttpMethod.GET, User.class, request, login);
    	
    	return new ResponseEntity<User>(response.getBody(), response.getStatusCode());
    }
}
