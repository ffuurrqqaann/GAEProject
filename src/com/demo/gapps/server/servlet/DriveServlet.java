package com.demo.gapps.server.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.demo.gapps.search.SearchIndexManager;
import com.demo.gapps.server.Utils;
import com.demo.gapps.shared.Constant;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

@SuppressWarnings({ "serial", "deprecation" })
public class DriveServlet extends HttpServlet {

	/** Global instance of Logger. */
	private final static Logger LOGGER = Logger.getLogger(DriveServlet.class.getName());
	
	/** Global instance of the  GoogleAuthorizationCodeFlow */
	GoogleAuthorizationCodeFlow flow = null;
	
	/** Global instance of HttpTransport  */
	HttpTransport httpTransport = new NetHttpTransport(); 
	
	/** Global instance of JsonFactory  */
	JsonFactory jsonFactory = new JacksonFactory(); 

	@Override
	public void init() throws ServletException {
		super.init();

		LOGGER.setLevel(Constant.LOG_LEVEL);
		LOGGER.info("Initializing FavoritesServlet Servlet");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		LOGGER.fine("Running DriveServlet");
		
		//create credentials object for authorization.
		Credential credential = Utils.getStoredCredential((String) request.getSession().getAttribute(Constant.AUTH_USER_ID), 
				(CredentialStore) request.getSession().getServletContext().getAttribute(Constant.GOOG_CREDENTIAL_STORE));
		
		//fetch the authorization code from the url.
		String code = request.getParameter("code");
		
		//storing the access token to the session.
		request.getSession().setAttribute("access_token", credential.getAccessToken());
		
		//if its a call back request then fetch the code to fetch Google Drive Files through API.
		if( code!=null ) {
			String authCode = code;
			
			//storing the received auth code to the session.
			request.getSession().setAttribute("driveAuthCode", authCode);

			//fetching GoogleCredential to pass it on to the Drive API.
			GoogleTokenResponse googleResponse = flow.newTokenRequest(authCode).setRedirectUri(Constant.GAPPS_DEMO_REDIRECT_URI).execute(); 
			GoogleCredential googleCredential = new GoogleCredential().setFromTokenResponse(googleResponse);

			//Create a new authorized API client 
			Drive driveService = new Drive.Builder(httpTransport, jsonFactory, googleCredential).build(); 
			
			//fetch the list of all the files from the Drive.
			List<File> result = new ArrayList<File>();
			Files.List req = null;

			try {
				req = driveService.files().list();
				
				//Query to fetch only files not folders and those files which are owned by the users.
				FileList files = req.setQ("'root' in parents and trashed=false and mimeType != 'application/vnd.google-apps.folder' and 'me' in owners").execute();
				result.addAll(files.getFiles());          
				req.setPageToken(files.getNextPageToken());
			} 
			catch (IOException e)
			{
				System.out.println("An error occurred: " + e);
				req.setPageToken(null);
			}
			
			//ImportFilesIntoIndex.loadData(result);
			SearchIndexManager.INSTANCE.loadData(result);
			
			//dispatch the request to Search Servlet.
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/html/search.jsp");
			dispatcher.forward(request, response);

		} else { 
			//get the authorization url
			String url = getAuthorizationUrl();
			
			//redirect response to the authorization url to get the auth code.
			response.sendRedirect(url);
		}
	}

	/**
	 * Retrieve the authorization url using client id, client secret and drive scopes.
	 *
	 * @param null
	 * @return authorization url (String).
	 */
	private String getAuthorizationUrl() throws IOException {
		
		//Preparing Google Authorization Code Flow Object.
		flow = new GoogleAuthorizationCodeFlow.Builder( 
				this.httpTransport, this.jsonFactory, Constant.GAPPS_DEMO_CLIENT_ID, Constant.GAPPS_DEMO_CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE)) 
		.setAccessType("online") 
		.setApprovalPrompt("auto").build();
		
		//New Authorization Url.
		String url = flow.newAuthorizationUrl().setRedirectUri(Constant.GAPPS_DEMO_REDIRECT_URI).build();

		return url;
	}

}