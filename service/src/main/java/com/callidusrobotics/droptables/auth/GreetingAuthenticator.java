package com.callidusrobotics.droptables.auth;

import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

// https://github.com/dropwizard/dropwizard/tree/master/dropwizard-example/src/main/java/com/example/helloworld
// http://java.dzone.com/articles/getting-started-dropwizard-0

// http://gary-rowe.com/agilestack/2012/12/12/dropwizard-with-openid/
// https://github.com/yammer/dropwizard-auth-ldap
public class GreetingAuthenticator implements Authenticator<BasicCredentials, User> {

	private String login;	    
	private String password;

	public GreetingAuthenticator(String login, String password) {
		this.login = login;
		this.password = password;
	}
	
	@Override
	public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
		if (password.equals(credentials.getPassword()) && login.equals(credentials.getUsername())) {
			return Optional.of(new User());
		} else {
			return Optional.absent();
		}
	}
}