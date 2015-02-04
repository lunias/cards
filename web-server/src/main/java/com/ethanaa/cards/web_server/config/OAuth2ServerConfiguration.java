package com.ethanaa.cards.web_server.config;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.ethanaa.cards.common.constant.AuthorityConstants;
import com.ethanaa.cards.common.constant.ResourceConstants;
import com.ethanaa.cards.web_server.security.AjaxLogoutSuccessHandler;
import com.ethanaa.cards.web_server.security.Http401UnauthorizedEntryPoint;
import com.ethanaa.cards.web_server.web.rest.interop.OAuthInterop;

@Configuration
public class OAuth2ServerConfiguration {
	
	@Configuration
	protected static class AdditionalConfiguration implements EnvironmentAware {		                 
        
		private static final String ENV_SERVERS = "servers.";
		private static final String PROP_OAUTH_HOST = "oauth.host";
		
        private RelaxedPropertyResolver propertyResolver;		
		
        @Inject 
        private DataSource oauthDataSource;             
        
        @Bean
		public TokenStore tokenStore() {
			return new JdbcTokenStore(oauthDataSource);
		}
        
        @Bean
        public OAuthInterop oauthInterop() {
        	return new OAuthInterop(propertyResolver.getProperty(PROP_OAUTH_HOST) + '/');
        }        

		@Override
		public void setEnvironment(Environment environment) {
			this.propertyResolver = new RelaxedPropertyResolver(environment, ENV_SERVERS);
		}
	}
	
    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Inject
        private Http401UnauthorizedEntryPoint authenticationEntryPoint;

        @Inject
        private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;
        
        @Inject
        private TokenStore tokenStore;
        
        @Override
        public void configure(ResourceServerSecurityConfigurer resources)
        		throws Exception {        	
        	
        	resources.resourceId(ResourceConstants.WEB_RESOURCE)
        			 .tokenStore(tokenStore);
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
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/logs/**").hasAnyAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/api/**").authenticated()
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
                .antMatchers("/api-docs/**").hasAuthority(AuthorityConstants.ADMIN)
                .antMatchers("/protected/**").authenticated();

        }                
    }    
}
