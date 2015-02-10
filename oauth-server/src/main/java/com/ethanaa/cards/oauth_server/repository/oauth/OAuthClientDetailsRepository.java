package com.ethanaa.cards.oauth_server.repository.oauth;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ethanaa.cards.oauth_server.domain.oauth.OAuthClientDetails;

public interface OAuthClientDetailsRepository extends JpaRepository<OAuthClientDetails, String> {

}
