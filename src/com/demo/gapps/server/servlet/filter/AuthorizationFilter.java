package com.demo.gapps.server.servlet.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.extensions.appengine.auth.oauth2.AppEngineCredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment;
import com.demo.gapps.domain.AppUser;
import com.demo.gapps.server.Utils;
import com.demo.gapps.server.Utils.NoRefreshTokenException;
import com.demo.gapps.shared.Constant;

public final class AuthorizationFilter implements Filter {

	/** Global instance of the FilterConfig.
	 *
	 */
	private FilterConfig filterConfig = null;
	
	/** Global instance of CredentialStore.
	 *
	 */
	private CredentialStore credentialStore;
	
	private final static Logger LOGGER = Logger.getLogger(AuthorizationFilter.class.getName());

	public void init(FilterConfig filterConfig) throws ServletException {

		LOGGER.setLevel(Constant.LOG_LEVEL);
		
		LOGGER.info("Initializing Authorization Filter");
		
		credentialStore = new AppEngineCredentialStore();
		
		this.filterConfig = filterConfig;
	}

	public void destroy() {
		this.filterConfig = null;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		if (filterConfig == null)
			return;
		
		LOGGER.info("Invoking Authorization Filter");
	
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
	
		HttpSession session = request.getSession();
		
		LOGGER.info("Destination URL is: " + request.getRequestURI());
		
		// if not present, add credential store to servlet context
		if (session.getServletContext().getAttribute(Constant.GOOG_CREDENTIAL_STORE) == null) {
			LOGGER.fine("Adding credential store to context " + credentialStore);
			session.getServletContext().setAttribute(Constant.GOOG_CREDENTIAL_STORE, credentialStore);
		}
		
		// if google user isn't in session, add it
		if (session.getAttribute(Constant.AUTH_USER_ID) == null) {
			
			UserService userService = UserServiceFactory.getUserService();
			User user = userService.getCurrentUser();
			
			session.setAttribute(Constant.AUTH_USER_ID, user.getUserId());
			session.setAttribute(Constant.AUTH_USER_NICKNAME, user.getNickname());
			
			// if not running on app engine prod, hard-code my email address for testing
			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
				session.setAttribute(Constant.AUTH_USER_EMAIL, user.getEmail());
			} else {
				session.setAttribute(Constant.AUTH_USER_EMAIL, "jeffdavisco@gmail.com");
			}
		}
		
		// determine whether active user credentials exist for the user
		try {
			Credential credentials = Utils.getActiveCredential(request, credentialStore);
		} catch (NoRefreshTokenException e1) {
			// if this catch block is entered, we need to perform the oauth process
			LOGGER.info("No user found - authorization URL is: " + e1.getAuthorizationUrl());
			response.sendRedirect(e1.getAuthorizationUrl());
			return;
		}
		
		chain.doFilter(request, response);
	}

}