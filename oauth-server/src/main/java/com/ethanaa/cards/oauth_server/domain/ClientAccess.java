package com.ethanaa.cards.oauth_server.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.ethanaa.cards.common.domain.AbstractAuditingEntity;

/**
 * A ClientAccess record maps a username to an accessible oauth2 client
 * 
 * @author Ethan Anderson
 *
 */
@Entity
@Table(name = "T_CLIENT_ACCESS")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ClientAccess extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 3057908554660818641L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
    @NotNull
    @Pattern(regexp = "^[a-z0-9]*$")
    @Size(min = 1, max = 50)
    @Column(length = 50, nullable = false)
	private String username;
	
    @NotNull
    @Size(min = 1, max = 255) 
    @Column(length = 255, nullable = false)
	private String client;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
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
		ClientAccess other = (ClientAccess) obj;
		if (client == null) {
			if (other.client != null)
				return false;
		} else if (!client.equals(other.client))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClientAccess [username=" + username + ", client=" + client
				+ "]";
	}		
}
