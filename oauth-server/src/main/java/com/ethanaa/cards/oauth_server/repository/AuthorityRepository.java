package com.ethanaa.cards.oauth_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ethanaa.cards.oauth_server.domain.Authority;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
