package com.ethanaa.cards.oauth_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ethanaa.cards.oauth_server.domain.ClientAccess;

/**
 * 
 * Spring Data JPA repository for the ClientAccess entity.
 * 
 * @author Ethan Anderson
 *
 */
public interface ClientAccessRepository extends JpaRepository<ClientAccess, Long> {

	List<ClientAccess> findAllByUsername(String username);
	
	ClientAccess findFirstByUsernameAndClient(String username, String client);
}
