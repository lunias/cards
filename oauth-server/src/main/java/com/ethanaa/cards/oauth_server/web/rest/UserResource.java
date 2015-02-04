package com.ethanaa.cards.oauth_server.web.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.ethanaa.cards.common.constant.AuthorityConstants;
import com.ethanaa.cards.common.constant.ScopeConstants;
import com.ethanaa.cards.common.domain.User;
import com.ethanaa.cards.oauth_server.repository.UserRepository;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Inject
    private UserRepository userRepository;

    /**
     * GET  /users/:login -> get the "login" user.
     */
    @RequestMapping(value = "/users/{login}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")    
    public User getUser(@PathVariable String login, HttpServletResponse response) {
    	
        log.debug("REST request to get User : {}", login);
        
        User user = userRepository.findOneByLogin(login);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        return user;
    }
    
    @RequestMapping(value = "/users/{login}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)    
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_DELETE + "')")
    public ResponseEntity<?> deleteUser(@PathVariable String login) {
    	
        log.debug("REST request to delete User : {}", login);
        
        User user = userRepository.findOneByLogin(login);
        if (user == null) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        userRepository.delete(user);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
