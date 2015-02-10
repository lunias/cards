package com.ethanaa.cards.oauth_server.config;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.ethanaa.cards.common.constant.AuthorityConstants;
import com.ethanaa.cards.common.constant.ResourceConstants;
import com.ethanaa.cards.oauth_server.security.AjaxLogoutSuccessHandler;
import com.ethanaa.cards.oauth_server.security.CustomJdbcClientDetailsService;
import com.ethanaa.cards.oauth_server.security.Http401UnauthorizedEntryPoint;

@Configuration
public class OAuth2ServerConfiguration {

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Inject
        private Http401UnauthorizedEntryPoint authenticationEntryPoint;

        @Inject
        private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

        	resources.resourceId(ResourceConstants.OAUTH_RESOURCE);
        }
        
        @Override
        public void configure(HttpSecurity http) throws Exception {
        	
            http
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
            .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(ajaxLogoutSuccessHandler)
            .and()
                .csrf()
                .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/authorize"))
                .disable()
                .headers()
                .frameOptions().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/oauth/token").permitAll()                
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/logs/**").hasAnyAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/api/**").authenticated()
                .antMatchers("/protected/**").authenticated()
                .antMatchers("/metrics/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/health/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/trace/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/dump/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/shutdown/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/beans/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/configprops/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/info/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/autoconfig/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/env/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/trace/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/api-docs/**").hasAuthority(AuthorityConstants.ADMIN);

        }
    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter implements EnvironmentAware {

        private static final String ENV_OAUTH = "authentication.clients.";
        
        private static final String OAUTH_CLIENTID = "oauth.clientid";
        private static final String OAUTH_SECRET = "oauth.secret";
        private static final String OAUTH_TOKEN_VALIDITY_SECONDS = "oauth.tokenValidityInSeconds";        
        
        private static final String WEB_CLIENTID = "web.clientid";
        private static final String WEB_SECRET = "web.secret";
        private static final String WEB_TOKEN_VALIDITY_SECONDS = "web.tokenValidityInSeconds";
        
        private static final String GAME_CLIENTID = "game.clientid";
        private static final String GAME_SECRET = "game.secret";
        private static final String GAME_TOKEN_VALIDITY_SECONDS = "game.tokenValidityInSeconds";        
        
        private RelaxedPropertyResolver propertyResolver;

        @Inject
        private DataSource dataSource;

        @Bean
        public TokenStore tokenStore() {
        	
            return new JdbcTokenStore(dataSource);
        }
        
        @Bean
        public AuthorizationServerTokenServices tokenServices() {
        	
        	DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        	
        	defaultTokenServices.setTokenStore(tokenStore());
        	defaultTokenServices.setAccessTokenValiditySeconds(1800);
        	defaultTokenServices.setRefreshTokenValiditySeconds(1800);
        	
        	return defaultTokenServices;
        }

        @Inject
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
                throws Exception {

            endpoints
            		.tokenServices(tokenServices())
                    .tokenStore(tokenStore())
                    .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        	
        	clients.withClientDetails(customClientDetailsService());
        	
//        	clients.jdbc(dataSource);
        	
//            clients
//                .inMemory()
//                	.withClient(propertyResolver.getProperty(OAUTH_CLIENTID))
//                		.resourceIds(ResourceConstants.OAUTH_RESOURCE)
//                		.scopes(OAUTH_READ, 
//                				OAUTH_WRITE, 
//                				OAUTH_DELETE)
//                		.autoApprove(true)
//                		.authorities(AuthorityConstants.USER)
//                		.authorizedGrantTypes("password")
//                		.secret(propertyResolver.getProperty(OAUTH_SECRET))
//                		.accessTokenValiditySeconds(propertyResolver.getProperty(OAUTH_TOKEN_VALIDITY_SECONDS, Integer.class, 1800))                
//                .and()
//                	.withClient(propertyResolver.getProperty(WEB_CLIENTID))
//                		.resourceIds(ResourceConstants.WEB_RESOURCE, ResourceConstants.OAUTH_RESOURCE)
//                		.scopes(WEB_READ, 
//                				WEB_WRITE, 
//                				OAUTH_READ, 
//                				OAUTH_WRITE)
//                		.autoApprove(true)
//                		.authorities(AuthorityConstants.USER)
//                		.authorizedGrantTypes("password")
//                		.secret(propertyResolver.getProperty(WEB_SECRET))
//                		.accessTokenValiditySeconds(propertyResolver.getProperty(WEB_TOKEN_VALIDITY_SECONDS, Integer.class, 1800))
//                .and()
//                	.withClient(propertyResolver.getProperty(GAME_CLIENTID))
//                		.resourceIds(ResourceConstants.GAME_RESOURCE, ResourceConstants.OAUTH_RESOURCE)
//                		.scopes(GAME_READ, 
//                				GAME_WRITE, 
//                				OAUTH_READ)
//                		.autoApprove(true)
//                		.authorities(AuthorityConstants.USER)
//                		.authorizedGrantTypes("password")
//                		.secret(propertyResolver.getProperty(GAME_SECRET))
//                		.accessTokenValiditySeconds(propertyResolver.getProperty(GAME_TOKEN_VALIDITY_SECONDS, Integer.class, 1800));
        }
        
        @Bean
        public ClientDetailsService customClientDetailsService() {
        	
        	return new CustomJdbcClientDetailsService();
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.propertyResolver = new RelaxedPropertyResolver(environment, ENV_OAUTH);
        }
    }
}
