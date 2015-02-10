package com.ethanaa.cards.oauth_server.domain.oauth;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "oauth_client_details")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class OAuthClientDetails implements Serializable {

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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getAuthorizedGrantTypes() {
		return authorizedGrantTypes;
	}

	public void setAuthorizedGrantTypes(String authorizedGrantTypes) {
		this.authorizedGrantTypes = authorizedGrantTypes;
	}

	public String getWebServerRedirectUri() {
		return webServerRedirectUri;
	}

	public void setWebServerRedirectUri(String webServerRedirectUri) {
		this.webServerRedirectUri = webServerRedirectUri;
	}

	public String getAuthorities() {
		return authorities;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public Integer getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(Integer accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public Integer getRefreshTokenValidity() {
		return refreshTokenValidity;
	}

	public void setRefreshTokenValidity(Integer refreshTokenValidity) {
		this.refreshTokenValidity = refreshTokenValidity;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public String getAutoApprove() {
		return autoApprove;
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
