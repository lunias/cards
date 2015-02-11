package com.ethanaa.cards.oauth_server.web.rest.oauth;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.ethanaa.cards.oauth_server.domain.Authority;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthClientDetails;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthResource;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthScope;

@Component
public class OAuthClientDetailsAssembler extends ResourceAssemblerSupport<OAuthClientDetails, OAuthClientDetailsResource> {

	public OAuthClientDetailsAssembler() {
		super(OAuthEndpoint.class, OAuthClientDetailsResource.class);
	}

	@Override
	public OAuthClientDetailsResource toResource(OAuthClientDetails entity) {
	
		OAuthClientDetailsResource resource = instantiateResource(entity);
		
		resource.setClientId(entity.getClientId());
		resource.setAuthorizedGrantTypes(entity.getAuthorizedGrantTypes());
		resource.setRedirectUrls(entity.getRegisteredRedirectUri());
		resource.setAccessTokenLifeSeconds(entity.getAccessTokenValiditySeconds());
		resource.setRefreshTokenLifeSeconds(entity.getRefreshTokenValiditySeconds());
		resource.setAdditionalInfo((String) entity.getAdditionalInformation().get("info"));
		resource.setAutoApprove(entity.isAutoApprove(""));
		
		Set<OAuthResource> resources = new HashSet<>();
		for (String resourceId : entity.getResourceIds()) {
			resources.add(new OAuthResource(resourceId));
		}
		
		resource.setResources(resources);
		
		Set<OAuthScope> scopes = new HashSet<>();
		for (String scope : entity.getScope()) {
			scopes.add(new OAuthScope(scope));
		}
		
		resource.setScopes(scopes);
		
		Set<Authority> authorities = new HashSet<>();
		for (GrantedAuthority grantedAuthority : entity.getAuthorities()) {
			authorities.add(new Authority(grantedAuthority));
		}
		
		resource.setAuthorities(authorities);
		
		resource.add(linkTo(OAuthEndpoint.class).slash("clients").slash(entity.getClientId()).withSelfRel());
		
		return resource;
	}		
}
