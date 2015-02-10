package com.ethanaa.cards.oauth_server.web.rest.user;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import com.ethanaa.cards.common.web.rest.resource.UserResource;
import com.ethanaa.cards.oauth_server.domain.Authority;
import com.ethanaa.cards.oauth_server.domain.User;

@Component
public class UserAssembler extends ResourceAssemblerSupport<User, UserResource> {

	public UserAssembler() {
		super(UserEndpoint.class, UserResource.class);
	}

	@Override
	public UserResource toResource(User entity) {
		
		UserResource resource = instantiateResource(entity);
		
		BeanUtils.copyProperties(entity, resource, "password");
		
		for (Authority auth : entity.getAuthorities()) {
			resource.getRoles().add(auth.getName());
		}		
		
		resource.add(linkTo(UserEndpoint.class).slash(entity.getLogin()).withSelfRel());
		
		return resource;
	}		

}
