package com.ethanaa.cards.oauth_server.web.rest.authority;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.ethanaa.cards.common.constant.AuthorityConstants;
import com.ethanaa.cards.common.constant.ScopeConstants;
import com.ethanaa.cards.oauth_server.domain.Authority;
import com.ethanaa.cards.oauth_server.repository.AuthorityRepository;

@RestController
@RequestMapping("/api/authorities")
public class AuthorityEndpoint {

	private final Logger log = LoggerFactory.getLogger(AuthorityEndpoint.class);
	
	@Inject
	private AuthorityRepository authorityRepository;
	
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthorityConstants.ADMIN)
    @PreAuthorize("#oauth2.hasScope('" + ScopeConstants.OAUTH_READ + "')")   	
	public ResponseEntity<List<Authority>> getAuthorities() {
		
        log.debug("REST request to get Authorities");
        
        List<Authority> authorities = authorityRepository.findAll();
        
        return new ResponseEntity<>(authorities, HttpStatus.OK);
	}
	
}
