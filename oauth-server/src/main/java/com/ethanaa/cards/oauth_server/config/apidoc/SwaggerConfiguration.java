package com.ethanaa.cards.oauth_server.config.apidoc;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;

import com.ethanaa.cards.common.constant.Constants;
import com.ethanaa.cards.common.constant.ScopeConstants;
import com.mangofactory.swagger.authorization.AuthorizationContext;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.models.dto.Authorization;
import com.mangofactory.swagger.models.dto.AuthorizationCodeGrant;
import com.mangofactory.swagger.models.dto.AuthorizationScope;
import com.mangofactory.swagger.models.dto.AuthorizationType;
import com.mangofactory.swagger.models.dto.GrantType;
import com.mangofactory.swagger.models.dto.ImplicitGrant;
import com.mangofactory.swagger.models.dto.LoginEndpoint;
import com.mangofactory.swagger.models.dto.OAuth;
import com.mangofactory.swagger.models.dto.TokenEndpoint;
import com.mangofactory.swagger.models.dto.TokenRequestEndpoint;
import com.mangofactory.swagger.models.dto.builder.ApiInfoBuilder;
import com.mangofactory.swagger.models.dto.builder.OAuthBuilder;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

/**
 * Swagger configuration.
 *
 * Warning! When having a lot of REST endpoints, Swagger can become a performance issue. In that
 * case, you can use a specific Spring profile for this class, so that only front-end developers
 * have access to the Swagger view.
 */
@Configuration
@EnableSwagger
@Profile("!" + Constants.SPRING_PROFILE_FAST)
public class SwaggerConfiguration implements EnvironmentAware {

    private final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

    public static final String DEFAULT_INCLUDE_PATTERN = "/api/.*";

    private RelaxedPropertyResolver propertyResolver;

    @Override
    public void setEnvironment(Environment environment) {
        this.propertyResolver = new RelaxedPropertyResolver(environment, "swagger.");
    }

    /**
     * Swagger Spring MVC configuration.
     */
    @Bean
    public SwaggerSpringMvcPlugin swaggerSpringMvcPlugin(SpringSwaggerConfig springSwaggerConfig) {
        log.debug("Starting Swagger");
        StopWatch watch = new StopWatch();
        watch.start();
        SwaggerSpringMvcPlugin swaggerSpringMvcPlugin = new SwaggerSpringMvcPlugin(springSwaggerConfig)
            .apiInfo(apiInfo())
            //.authorizationTypes(authorizationTypes())
            //.authorizationContext(authorizationContext())
            .genericModelSubstitutes(ResponseEntity.class)
            .includePatterns(DEFAULT_INCLUDE_PATTERN);

        swaggerSpringMvcPlugin.build();
        watch.stop();
        log.debug("Started Swagger in {} ms", watch.getTotalTimeMillis());
        return swaggerSpringMvcPlugin;
    }

    /**
     * API Info as it appears on the swagger-ui page.
     */
    private ApiInfo apiInfo() {
    	
    	return new ApiInfoBuilder()
    				.title(propertyResolver.getProperty("title"))
    				.description(propertyResolver.getProperty("description"))
    				.termsOfServiceUrl(propertyResolver.getProperty("termsOfServiceUrl"))
    				.contact(propertyResolver.getProperty("contact"))
    				.license(propertyResolver.getProperty("license"))
    				.licenseUrl(propertyResolver.getProperty("licenseUrl"))
    				.build();    	
    }
    
//	private List<AuthorizationType> authorizationTypes() {
//		ArrayList<AuthorizationType> authorizationTypes = new ArrayList<AuthorizationType>();
//
//		List<AuthorizationScope> authorizationScopeList = new ArrayList<>();
//		authorizationScopeList.add(new AuthorizationScope(ScopeConstants.OAUTH_READ, "read access to oauth resource"));
//
//		List<GrantType> grantTypes = new ArrayList<>();
//
//		LoginEndpoint loginEndpoint = new LoginEndpoint(
//				"/api/oauth/token");
//		grantTypes.add(new ImplicitGrant(loginEndpoint, "access_token"));
//
//		TokenRequestEndpoint tokenRequestEndpoint = new TokenRequestEndpoint(
//				"/api/oauth/token",
//				"cards-oauth", "");
//		TokenEndpoint tokenEndpoint = new TokenEndpoint(
//				"/api/oauth/token", "value");
//
//		AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(
//				tokenRequestEndpoint, tokenEndpoint);
//		grantTypes.add(authorizationCodeGrant);
//
//		OAuth oAuth = new OAuthBuilder().scopes(authorizationScopeList)
//				.grantTypes(grantTypes).build();
//
//		authorizationTypes.add(oAuth);
//		return authorizationTypes;
//	}
//
//	@Bean
//	public AuthorizationContext authorizationContext() {
//		List<Authorization> authorizations = new ArrayList<>();
//
//		AuthorizationScope authorizationScope = new AuthorizationScope(
//				ScopeConstants.OAUTH_READ, "read access to oauth resource");
//		AuthorizationScope[] authorizationScopes = new AuthorizationScope[] { authorizationScope };
//		authorizations.add(new Authorization("oauth2", authorizationScopes));
//		AuthorizationContext authorizationContext = new AuthorizationContext.AuthorizationContextBuilder(
//				authorizations).build();
//		return authorizationContext;
//	}        
}
