package com.demo.gapps.server;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.common.base.Preconditions;
import com.demo.gapps.shared.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("deprecation")
public class Utils {

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			System.getProperty("user.home"), "./credentials/GoogleDriveCredentials.json");

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY =
			JacksonFactory.getDefaultInstance();

	/** Global instance of the scopes required by this GappsDemo Application.
	 *
	 */
	private static final List<String> SCOPES =
			Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY);

	/** Global instance of the scopes required by this GappsDemo Application.
	 *
	 */
	private static GoogleClientSecrets clientSecrets = null;

	/**
     * @return a GoogleClientSecrets object.
     * @throws IOException
     */
	public static GoogleClientSecrets getClientCredential() throws IOException {
		if (clientSecrets == null) {
			InputStream inputStream = Utils.class
					.getResourceAsStream(Constant.AUTH_RESOURCE_LOC);
			Preconditions.checkNotNull(inputStream, "missing resource %s",
					Constant.AUTH_RESOURCE_LOC);

			clientSecrets = GoogleClientSecrets.load(Constant.JSON_FACTORY, new InputStreamReader(
					Utils.class.getResourceAsStream(Constant.AUTH_RESOURCE_LOC), "UTF-8"));

			Preconditions
			.checkArgument(
					!clientSecrets.getDetails().getClientId().startsWith("[[")
					&& !clientSecrets.getDetails().getClientSecret()
					.startsWith("[["),
					"Please enter your client ID and secret from the Google APIs Console in %s from the "
							+ "root samples directory", Constant.AUTH_RESOURCE_LOC);
		}
		return clientSecrets;
	}

	/**
     * @return a Credential object.
     * @throws IOException
     */
	public static Credential getStoredCredential(String userId, CredentialStore credentialStore) throws IOException {
		Credential credential = buildEmptyCredential();
		if (credentialStore.load(userId, credential)) {
			return credential;
		}
		return null;
	}

	/**
     * @return a Credential object.
     * @throws IOException
     */
	public static Credential getActiveCredential(HttpServletRequest request, CredentialStore credentialStore) throws NoRefreshTokenException, IOException {

		String userId = (String) request.getSession().getAttribute(Constant.AUTH_USER_ID);
		Credential credential = null;

		try {
			// Only bother looking for a Credential if the user has an existing
			// session with their email address stored.
			if (userId != null) {
				credential = getStoredCredential(userId, credentialStore);
			}

			// No Credential was stored for the current user or no refresh token is
			// available.
			// If an authorizationCode is present, upgrade it into an
			// access token and hopefully a refresh token.
			if ((credential == null || credential.getRefreshToken() == null) && request.getSession().getAttribute("code") != null) {
				credential = exchangeCode((String) request.getSession().getAttribute("code"));

				if (credential != null) {
					if (credential.getRefreshToken() != null) {
						credentialStore.store(userId, credential);
					}
				}
			}

			if (credential == null || credential.getRefreshToken() == null) {
				// No refresh token has been retrieved.
				// Start a "fresh" OAuth 2.0 flow so that we can get a refresh token.
				String email = (String) request.getSession().getAttribute(Constant.AUTH_USER_EMAIL);
				String authorizationUrl = getAuthorizationUrl(email, request);
				throw new NoRefreshTokenException(authorizationUrl);
			}

		} catch (CodeExchangeException e) {
			// The code the user arrived here with was bad. This pretty much never
			// happens. In a production application, we'd either redirect the user
			// somewhere like a home page, or show them a vague error mentioning
			// that they probably didn't arrive to our app from Google Drive.
			e.printStackTrace();
		} 

		return credential;
	}

	/**
     * @return a Credential object.
     * @throws IOException
     */
	public static Credential refreshToken(HttpServletRequest request, Credential credential) {
		try {

			if (credential == null)
				return null;

			credential.refreshToken();

			CredentialStore store = (CredentialStore) request.getSession().getServletContext().getAttribute(Constant.GOOG_CREDENTIAL_STORE);

			store.store((String) request.getSession().getAttribute(Constant.AUTH_USER_ID), credential);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return credential;
	}

	/**
	 * @return an authorized url (String).
	 * @throws IOException
	 */
	public static String getAuthorizationUrl(String emailAddress, HttpServletRequest request) {
		// Generate an authorization URL based on our client settings,
		// the user's email address, and the state parameter, if present.
		GoogleAuthorizationCodeRequestUrl urlBuilder = null;
		try {
			urlBuilder = new GoogleAuthorizationCodeRequestUrl(
					getClientCredential().getWeb().getClientId(),
					Constant.OATH_CALLBACK_LIVE, Constant.SCOPES)
			.setAccessType("offline")
			.setApprovalPrompt("force");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Propagate through the current state parameter, so that when the
		// user gets redirected back to our app, they see the file(s) they
		// were originally supposed to see before we realized we weren't
		// authorized.
		urlBuilder.set("state", request.getRequestURI());

		if (emailAddress != null) {
			urlBuilder.set("user_id", emailAddress);
		}

		return urlBuilder.build();
	}


	/**
	 * Returns an {@link IOException} (but not a subclass) in order to work around
	 * restrictive GWT serialization policy.
	 */
	static IOException wrappedIOException(IOException e) {
		if (e.getClass() == IOException.class) {
			return e;
		}
		return new IOException(e.getMessage());
	}

	private Utils() {
	}

	/**
	 * @return a Credential object.
	 * @throws IOException
	 */
	public static Credential buildEmptyCredential() {
		try {
			return new GoogleCredential.Builder()
			.setClientSecrets(Utils.getClientCredential())
			.setTransport(new NetHttpTransport())
			.setJsonFactory(Constant.JSON_FACTORY).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class NoRefreshTokenException extends Exception {

		/**
		 * Authorization URL to which to redirect the user.
		 */
		private String authorizationUrl;

		/**
		 * Construct a NoRefreshTokenException.
		 * 
		 * @param authorizationUrl
		 *          The authorization URL to redirect the user to.
		 */
		public NoRefreshTokenException(String authorizationUrl) {
			this.authorizationUrl = authorizationUrl;
		}

		/**
		 * @return Authorization URL to which to redirect the user.
		 */
		public String getAuthorizationUrl() {
			return authorizationUrl;
		}
	}

	/**
	 * @return a Credential object.
	 * @throws IOException
	 */
	public static Credential exchangeCode(String authorizationCode)
			throws CodeExchangeException {
		// Talk to Google and upgrade the given authorization code to an access
		// token and hopefully a refresh token.

		try {
			GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
					new NetHttpTransport(), Constant.JSON_FACTORY, Utils
					.getClientCredential().getWeb().getClientId(), Utils
					.getClientCredential().getWeb().getClientSecret(),
					authorizationCode, Constant.OATH_CALLBACK_LIVE).execute();
			return Utils.buildEmptyCredential().setFromTokenResponse(response);
		} catch (IOException e) {
			e.printStackTrace();
			throw new CodeExchangeException();
		}
	}

	/**
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {

		System.out.println("file path is "+DATA_STORE_DIR.getPath());

		// Load client secrets.
		InputStream in =
				Utils.class.getResourceAsStream(Constant.AUTH_RESOURCE_LOC);
		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), JSON_FACTORY, clientSecrets, Arrays.asList(DriveScopes.DRIVE));

		java.io.File cFile = new java.io.File("/oauth2.json");
		cFile = cFile.getAbsoluteFile();

		FileCredentialStore credentialStore = new FileCredentialStore(cFile, JSON_FACTORY);
		builder.setCredentialStore(credentialStore);
		GoogleAuthorizationCodeFlow flow = builder.build();

		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	public static class CodeExchangeException extends Exception {
	}

	public static class NoUserIdException extends Exception {
	}

}