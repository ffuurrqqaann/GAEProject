package com.demo.gapps.domain;

import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * An application user, named with a prefix to avoid confusion with GAE User type
 */
@Entity
public class AppUser extends DatastoreObject
{
	private String email;
	private Date lastLogin;
	private Date firstLogin;
	
	@Embedded OauthCredentials credentials;

	public AppUser()
	{
		// No-arg constructor required by Objectify
	}
	
	public AppUser(String userEmail)
	{
		this.email = userEmail;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public OauthCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(OauthCredentials credentials) {
		this.credentials = credentials;
	}

	public Date getFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(Date firstLogin) {
		this.firstLogin = firstLogin;
	}

	public static class OauthCredentials {
		String accessToken;
		String accessTokenSecret;
		
		public OauthCredentials(){};
		
		public OauthCredentials(String accessToken, String accessTokenSecret) {
			super();
			this.accessToken = accessToken;
			this.accessTokenSecret = accessTokenSecret;
		}

		public String getAccessToken() {
			return accessToken;
		}
		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
		public String getAccessTokenSecret() {
			return accessTokenSecret;
		}
		public void setAccessTokenSecret(String accessTokenSecret) {
			this.accessTokenSecret = accessTokenSecret;
		}
		@Override
		public String toString() {
			return "OauthCredentials [accessToken=" + accessToken
					+ ", accessTokenSecret=" + accessTokenSecret + "]";
		}
	}

	@Override
	public String toString() {
		return "AppUser [email=" + email + ", lastLogin=" + lastLogin
				+ ", firstLogin=" + firstLogin + ", credentials=" + credentials
				+ "]";
	}
	
}
