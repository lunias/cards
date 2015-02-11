package com.ethanaa.cards.oauth_server.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ethanaa.cards.common.web.rest.exception.CurrentUserDeletionException;
import com.ethanaa.cards.common.web.rest.exception.EmailAlreadyExistsException;
import com.ethanaa.cards.common.web.rest.exception.UserNotFoundException;
import com.ethanaa.cards.common.web.rest.resource.UserResource;
import com.ethanaa.cards.oauth_server.domain.Authority;
import com.ethanaa.cards.oauth_server.domain.User;
import com.ethanaa.cards.oauth_server.domain.oauth.OAuthClientDetails;
import com.ethanaa.cards.oauth_server.repository.AuthorityRepository;
import com.ethanaa.cards.oauth_server.repository.UserRepository;
import com.ethanaa.cards.oauth_server.repository.oauth.OAuthClientDetailsRepository;
import com.ethanaa.cards.oauth_server.security.SecurityUtils;
import com.ethanaa.cards.oauth_server.service.util.RandomUtil;
import com.ethanaa.cards.oauth_server.web.rest.oauth.OAuthClientDetailsResource;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuthorityRepository authorityRepository;
    
    @Inject
    private OAuthClientDetailsRepository oauthClientDetailsRepository;
    
    @Transactional(readOnly = true)    
    public Page<User> getUsersWithAuthorities(Pageable pageable) {
    	
    	Page<User> page = userRepository.findAll(pageable);
    	
    	for (User user : page.getContent()) {
    		user.getAuthorities().size(); // load many-to-many
    	}
    	
    	return page;
    }
    
    @Transactional(readOnly = true)   
    public User getUserWithAuthorities(Long id) throws UserNotFoundException {
    	
    	User user = userRepository.findOne(id);
    	
    	if (user == null) {
    		throw new UserNotFoundException(Long.toString(id));
    	}
    	
    	user.getAuthorities().size(); // load many-to-many
    	
    	return user;
    }
    
    @Transactional(readOnly = true)
    public User getUserWithAuthorities(String username) throws UserNotFoundException {
    	
    	User user = userRepository.findOneByLogin(username);
    	
    	if (user == null) {
    		throw new UserNotFoundException(username);
    	}
    	
    	user.getAuthorities().size();
    	
    	return user;
    }
    
    @Transactional(readOnly = true)
    public User getUserWithAuthorities() throws UserNotFoundException {
    	
    	String currentLogin = SecurityUtils.getCurrentLogin();
    	
        User currentUser = userRepository.findOneByLogin(currentLogin);
        
        if (currentUser == null) {
        	throw new UserNotFoundException(currentLogin);
        }
        
        currentUser.getAuthorities().size(); // load many-to-many
        
        return currentUser;
    }
    
    @Transactional(readOnly = true)
    public Set<OAuthClientDetails> getUserClientDetails(String username) throws UserNotFoundException {
    	
    	User user = userRepository.findOneByLogin(username);
    	
    	if (user == null) {
    		throw new UserNotFoundException(username);
    	}
    	
    	user.getClientDetails().size();
    	
    	return user.getClientDetails();    	    	
    }
    
    public SimpleEntry<List<OAuthClientDetails>, Boolean> addOrCreateUserClientDetails(String username, OAuthClientDetailsResource resource) throws UserNotFoundException {
    	
    	User user = userRepository.findOneByLogin(username);
    	
    	if (user == null) {
    		throw new UserNotFoundException(username);
    	}
    	
    	OAuthClientDetails existingClient = oauthClientDetailsRepository.findOne(resource.getClientId());
    	
    	if (existingClient == null) {    		
    		
    		existingClient = oauthClientDetailsRepository.save(new OAuthClientDetails(resource));    		
    	}
    		
    	user.getClientDetails().add(existingClient);
    	// TODO save user?
    	
    	return new SimpleEntry<List<OAuthClientDetails>, Boolean>(new ArrayList<>(user.getClientDetails()), false);
    }
    
    public User updateUser(UserResource userResource) throws UserNotFoundException, EmailAlreadyExistsException {
    	
    	String requestedEmail = userResource.getEmail();
    	String requestedLogin = userResource.getLogin();
    	
    	User user = getUserWithAuthorities(requestedLogin);    	
    	if (user == null) {
    		throw new UserNotFoundException(requestedLogin);
    	}    	    
    	
    	User existingEmailUser = userRepository.findOneByEmail(userResource.getEmail());
    	if (existingEmailUser != null && !requestedEmail.equals(user.getEmail())) {
    		throw new EmailAlreadyExistsException(requestedEmail);
    	}
    	
    	user.setFirstName(userResource.getFirstName());
    	user.setLastName(userResource.getLastName());
    	user.setEmail(requestedEmail);
    	
    	Set<Authority> validAuthorities = new HashSet<>();
    	for (String role : userResource.getRoles()) {
    		
    		Authority authority = authorityRepository.findOne(role);    		
    		if (authority != null) {
        		validAuthorities.add(authority);	
    		}
    	}
    	
    	user.setAuthorities(validAuthorities);
    	
    	return userRepository.save(user);
    }
    
    public void deleteUser(String username) throws UserNotFoundException {
    	
    	if (username.equals(SecurityUtils.getCurrentLogin())) {
    		throw new CurrentUserDeletionException(username);
    	}
    	
    	User user = userRepository.findOneByLogin(username);
    	
    	if (user == null) {
    		throw new UserNotFoundException(username);
    	}
    	
    	userRepository.delete(user);    	
    }
    
    public  User activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        User user = userRepository.findOneByActivationKey(key);
        // activate given user for the registration key.
        if (user != null) {
            user.setActivated(true);
            user.setActivationKey(null);
            userRepository.save(user);
            log.debug("Activated user: {}", user);
        }
        return user;
    }

    public User createUserInformation(String login, String password, String firstName, String lastName, String email,
                                      String langKey) {
        User newUser = new User();
        Authority authority = authorityRepository.findOne("ROLE_USER");
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public void updateUserInformation(String firstName, String lastName, String email) {
        User currentUser = userRepository.findOneByLogin(SecurityUtils.getCurrentLogin());
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        userRepository.save(currentUser);
        log.debug("Changed Information for User: {}", currentUser);
    }

    public void changePassword(String password) {
        User currentUser = userRepository.findOneByLogin(SecurityUtils.getCurrentLogin());
        String encryptedPassword = passwordEncoder.encode(password);
        currentUser.setPassword(encryptedPassword);
        userRepository.save(currentUser);
        log.debug("Changed password for User: {}", currentUser);
    }


    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p/>
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        DateTime now = new DateTime();
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
        }
    }
}
