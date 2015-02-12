package com.ethanaa.cards.oauth_server.web.rest.user;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Set;

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
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthClientDetails;
import com.ethanaa.cards.oauth_server.service.UserService;
import com.ethanaa.cards.oauth_server.web.rest.oauth.OAuthClientDetailsAssembler;
import com.ethanaa.cards.oauth_server.web.rest.oauth.OAuthClientDetailsResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * REST controller for managing users.
 */
@Api(value = "user-endpoint", description = "/api/users")
@RestController
@RequestMapping("/api/users")
public class UserEndpoint {

    private final Logger log = LoggerFactory.getLogger(UserEndpoint.class);
    
    @Inject
    private UserService userService;
    
    @Inject
    private UserAssembler userAssembler;
    
    @Inject
    private OAuthClientDetailsAssembler oauthClientDetailsAssembler;

    @ApiOperation(value = "Get all users",
    		notes = "This is the endpoint for obtaining information about the users "
    			  + "which currently exist in the system."
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 401, message = "The user is not authenticated"),
    		@ApiResponse(code = 403, message = "The user is not authorized")
    })       
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", defaultValue = "0", dataType = "int", required = false),		
		@ApiImplicitParam(name = "size", defaultValue = "20", dataType = "int", required = false),
		@ApiImplicitParam(name = "sort", defaultValue = "0", dataType = "Sort", required = false)		
	})
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
    
    @ApiOperation(value = "Get a user by id",
    		notes = "This is the endpoint for obtaining information about a user "
    			  + "which currently exist in the system."
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 401, message = "The user is not authenticated"),
    		@ApiResponse(code = 403, message = "The user is not authorized"),
    		@ApiResponse(code = 404, message = "No user exists with the provided id")    		
    })        
    @RequestMapping(value = "/byId/{id}",
    		method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")       
    public ResponseEntity<UserResource> getUser(
    		@ApiParam(value = "user id", required = true)
    		@PathVariable Long id) {
    	
        log.debug("REST request to get User by id : {}", id);
        
        User user = userService.getUserWithAuthorities(id);
        
        return new ResponseEntity<>(userAssembler.toResource(user), HttpStatus.OK);
    }
    
    @ApiOperation(value = "Get a user by login",
    		notes = "This is the endpoint for obtaining information about a user "
    			  + "which currently exist in the system."
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 401, message = "The user is not authenticated"),
    		@ApiResponse(code = 403, message = "The user is not authorized"),
    		@ApiResponse(code = 404, message = "No user exists with the provided login")    		
    })      
    @RequestMapping(value = "/{login}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")    
    public ResponseEntity<UserResource> getUser(
    		@ApiParam(value = "user login", required = true)
    		@PathVariable String login) {
    	
        log.debug("REST request to get User : {}", login);
        
        User user = userService.getUserWithAuthorities(login);
        
        return new ResponseEntity<>(userAssembler.toResource(user), HttpStatus.OK);
    }
    
    @ApiOperation(value = "Update a user",
    		notes = "This is the endpoint for modifying users."
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 401, message = "The user is not authenticated"),
    		@ApiResponse(code = 403, message = "The user is not authorized"),
    		@ApiResponse(code = 404, message = "No user exists with the provided login")    		
    })         
    @RequestMapping(value = "/{login}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_WRITE + "')")    
    public ResponseEntity<UserResource> updateUser(
    		@ApiParam(value = "user login", required = true)
    		@PathVariable String login,
    		@ApiParam(value = "user", required = true)
    		@RequestBody UserResource userResource) {
    	
        log.debug("REST request to update User : {}", login);                
        
        User user = userService.updateUser(userResource);
        
        return new ResponseEntity<>(userAssembler.toResource(user), HttpStatus.OK);
    }    
    
    @ApiOperation(value = "Delete a user",
    		notes = "This is the endpoint for deleting users."
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "The user was found and deleted"),
    		@ApiResponse(code = 401, message = "The user is not authenticated"),
    		@ApiResponse(code = 403, message = "The user is not authorized"),
    		@ApiResponse(code = 404, message = "No user exists with the provided login")    		
    })      
    @RequestMapping(value = "/{login}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)    
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_DELETE + "')")
    public ResponseEntity<?> deleteUser(
    		@ApiParam(value = "user login", required = true)
    		@PathVariable String login) {
    	
        log.debug("REST request to delete User : {}", login);
        
        userService.deleteUser(login);        
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @ApiOperation(value = "Get a user's oauth clients",
    		notes = "This is the endpoint for obtaining information about a user's "
    			  + "oauth clients."
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 401, message = "The user is not authenticated"),
    		@ApiResponse(code = 403, message = "The user is not authorized"),
    		@ApiResponse(code = 404, message = "No user exists with the provided login")    		
    })      
    @RequestMapping(value = "/{login}/clients",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")       
    public ResponseEntity<List<OAuthClientDetailsResource>> getClientDetails(
    		@ApiParam(value = "user login", required = true)
    		@PathVariable String login) {
    	
    	Set<OAuthClientDetails> clientDetails = userService.getUserClientDetails(login);    	    	
    	
        return new ResponseEntity<>(oauthClientDetailsAssembler.toResources(clientDetails), HttpStatus.OK);    	
    }
    
    @ApiOperation(value = "Add an oauth client to a user",
    		notes = "This is the endpoint for adding oauth clients "
    			  + "to a user."
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "The client was found and added"),
    		@ApiResponse(code = 201, message = "A new client was created and added"),    		
    		@ApiResponse(code = 401, message = "The user is not authenticated"),
    		@ApiResponse(code = 403, message = "The user is not authorized"),
    		@ApiResponse(code = 404, message = "No user exists with the provided login")    		
    })      
    @RequestMapping(value = "/{login}/clients",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_WRITE + "')")       
    public ResponseEntity<List<OAuthClientDetailsResource>> postClientDetails(
    		@ApiParam(value = "user login", required = true)
    		@PathVariable String login, 
    		@ApiParam(value = "oauth client details", required = true)
    		@RequestBody OAuthClientDetailsResource request) {
    	
    	SimpleEntry<List<OAuthClientDetails>, Boolean> clientDetails = userService.addOrCreateUserClientDetails(login, request);
    	
    	return new ResponseEntity<>(oauthClientDetailsAssembler.toResources(clientDetails.getKey()),
    			clientDetails.getValue() ? HttpStatus.CREATED : HttpStatus.OK);
    }
    
    @ApiOperation(value = "Remove an oauth client from a user",
    		notes = "This is the endpoint for removing oauth clients "
    			  + "from a user."    		
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "The user's client was removed"),
    		@ApiResponse(code = 401, message = "The user is not authenticated"),
    		@ApiResponse(code = 403, message = "The user is not authorized"),
    		@ApiResponse(code = 404, message = "No user exists with the provided login")    		
    })      
    @RequestMapping(value = "/{login}/clients/{clientId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)    
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_DELETE + "')")    
    public ResponseEntity<?> deleteClientDetails(
    		@ApiParam(value = "user login", required = true)
    		@PathVariable String login, 
    		@ApiParam(value = "oauth client id", required = true)    		
    		@PathVariable String clientId) {
    	
    	userService.removeUserClientDetails(login, clientId);
    	
    	return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
