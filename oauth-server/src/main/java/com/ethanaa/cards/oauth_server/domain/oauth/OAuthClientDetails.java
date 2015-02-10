package com.ethanaa.cards.oauth_server.domain.oauth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "oauth_client_details")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class OAuthClientDetails implements ClientDetails, Serializable {

	private static final long serialVersionUID = 4049425310865326805L;
	
    @NotNull
    @Size(min = 0, max = 256)
    @Id
    @Column(name = "client_id", length = 256)
	private String clientId;
    
    @Column(name = "resource_ids")
    private String resourceIds;
    
    @Column(name = "client_secret")
    private String clientSecret;
    
    @Column(name = "scope")
    private String scope;
    
    @Column(name = "authorized_grant_types")
    private String authorizedGrantTypes;
    
    @Column(name = "web_server_redirect_uri")
    private String webServerRedirectUri;
    
    @Column(name = "authorities")
    private String authorities;
    
    @Column(name = "access_token_validity")
    private Integer accessTokenValidity;
    
    @Column(name = "refresh_token_validity")
    private Integer refreshTokenValidity;
    
    @Size(min = 0, max = 4096)
    @Column(name = "additional_information")
    private String additionalInformation;
    
    @Size(min = 0, max = 256)
    @Column(name = "autoapprove")
    private String autoApprove;

    public OAuthClientDetails() {
	}
    
    public OAuthClientDetails(ClientDetails clientDetails) {
    	
    	this.clientId = clientDetails.getClientId();
    	this.resourceIds = StringUtils.collectionToCommaDelimitedString(clientDetails.getResourceIds());
    	this.clientSecret = clientDetails.getClientSecret();
    	this.scope = StringUtils.collectionToCommaDelimitedString(clientDetails.getScope());
    	this.authorizedGrantTypes = StringUtils.collectionToCommaDelimitedString(clientDetails.getAuthorizedGrantTypes());
    	this.webServerRedirectUri = StringUtils.collectionToCommaDelimitedString(clientDetails.getRegisteredRedirectUri());
    	this.authorities = StringUtils.collectionToCommaDelimitedString(clientDetails.getAuthorities());
    	this.accessTokenValidity = clientDetails.getAccessTokenValiditySeconds();
    	this.refreshTokenValidity = clientDetails.getRefreshTokenValiditySeconds();
    	this.autoApprove = "TRUE";
    }

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public Set<String> getResourceIds() {
		return StringUtils.commaDelimitedListToSet(resourceIds);
	}

	@Override
	public boolean isSecretRequired() {
		return true;
	}

	@Override
	public String getClientSecret() {
		return clientSecret;
	}

	@Override
	public boolean isScoped() {
		return true;
	}

	@Override
	public Set<String> getScope() {
		return StringUtils.commaDelimitedListToSet(scope);
	}

	@Override
	public Set<String> getAuthorizedGrantTypes() {
		return StringUtils.commaDelimitedListToSet(authorizedGrantTypes);
	}

	@Override
	public Set<String> getRegisteredRedirectUri() {
		return StringUtils.commaDelimitedListToSet(webServerRedirectUri);
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		for (String authority : StringUtils.commaDelimitedListToSet(authorities)) {
			grantedAuthorities.add(new SimpleGrantedAuthority(authority));
		}
		
		return grantedAuthorities;
	}

	@Override
	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValidity;
	}

	@Override
	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValidity;
	}

	@Override
	public boolean isAutoApprove(String scope) {
		return Boolean.parseBoolean(autoApprove);
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String, Object> getAdditionalInformation() {
		return new HashMap<String, Object>() {
			{
				put("info", additionalInformation);
			}
		};
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setAuthorizedGrantTypes(String authorizedGrantTypes) {
		this.authorizedGrantTypes = authorizedGrantTypes;
	}

	public void setWebServerRedirectUri(String webServerRedirectUri) {
		this.webServerRedirectUri = webServerRedirectUri;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public void setAccessTokenValidity(Integer accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public void setRefreshTokenValidity(Integer refreshTokenValidity) {
		this.refreshTokenValidity = refreshTokenValidity;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public void setAutoApprove(String autoApprove) {
		this.autoApprove = autoApprove;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientId == null) ? 0 : clientId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OAuthClientDetails other = (OAuthClientDetails) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OAuthClientDetails [clientId=" + clientId + ", resourceIds="
				+ resourceIds + ", clientSecret=" + clientSecret + ", scope="
				+ scope + ", authorizedGrantTypes=" + authorizedGrantTypes
				+ ", webServerRedirectUri=" + webServerRedirectUri
				+ ", authorities=" + authorities + ", accessTokenValidity="
				+ accessTokenValidity + ", refreshTokenValidity="
				+ refreshTokenValidity + ", additionalInformation="
				+ additionalInformation + ", autoApprove=" + autoApprove + "]";
	}		
}
