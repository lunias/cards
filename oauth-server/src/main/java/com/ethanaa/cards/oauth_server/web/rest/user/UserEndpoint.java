package com.ethanaa.cards.oauth_server.web.rest.user;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.ethanaa.cards.common.constant.AuthorityConstants;
import com.ethanaa.cards.common.constant.ScopeConstants;
import com.ethanaa.cards.common.web.rest.resource.UserResource;
import com.ethanaa.cards.oauth_server.domain.User;
import com.ethanaa.cards.oauth_server.service.UserService;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api/users")
public class UserEndpoint {

    private final Logger log = LoggerFactory.getLogger(UserEndpoint.class);
    
    @Inject
    private UserService userService;
    
    @Inject
    private UserAssembler userAssembler;

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")     
    public ResponseEntity<PagedResources<UserResource>> getUsers(Pageable pageable, PagedResourcesAssembler<User> assembler) {
    	
        log.debug("REST request to get Users");
        
        Page<User> page = userService.getUsersWithAuthorities(pageable);
        
        return new ResponseEntity<>(assembler.toResource(page, userAssembler), HttpStatus.OK);
    }
    
    @RequestMapping(value = "/byId/{id}",
    		method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")       
    public ResponseEntity<UserResource> getUser(@PathVariable Long id) {
    	
        log.debug("REST request to get User by id : {}", id);
        
        User user = userService.getUserWithAuthorities(id);
        
        return new ResponseEntity<>(userAssembler.toResource(user), HttpStatus.OK);
    }
    
    /**
     * GET  /users/:login -> get the "login" user.
     */
    @RequestMapping(value = "/{login}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")    
    public ResponseEntity<UserResource> getUser(@PathVariable String login) {
    	
        log.debug("REST request to get User : {}", login);
        
        User user = userService.getUserWithAuthorities(login);
        
        return new ResponseEntity<>(userAssembler.toResource(user), HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{login}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_WRITE + "')")    
    public ResponseEntity<UserResource> updateUser(@PathVariable String login, @RequestBody UserResource userResource) {
    	
        log.debug("REST request to update User : {}", login);                
        
        User user = userService.updateUser(userResource);
        
        return new ResponseEntity<>(userAssembler.toResource(user), HttpStatus.OK);
    }    
    
    @RequestMapping(value = "/{login}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)    
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_DELETE + "')")
    public ResponseEntity<?> deleteUser(@PathVariable String login) {
    	
        log.debug("REST request to delete User : {}", login);
        
        userService.deleteUser(login);        
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
