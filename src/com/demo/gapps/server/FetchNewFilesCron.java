package com.demo.gapps.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.demo.gapps.search.SearchIndexManager;
import com.demo.gapps.shared.Constant;
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
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;


@SuppressWarnings("serial")
public class FetchNewFilesCron extends HttpServlet {

	/** Global instance of the GoogleAuthorizationCodeFlow */
	GoogleAuthorizationCodeFlow flow = null;

	/** Global instance of HttpTransport  */
	HttpTransport httpTransport = new NetHttpTransport(); 

	/** Global instance of JsonFactory  */
	JsonFactory jsonFactory = new JacksonFactory(); 

	private final static Logger LOGGER = Logger.getLogger(FetchNewFilesCron.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();

		LOGGER.setLevel(Constant.LOG_LEVEL);
		LOGGER.info("Initializing FetchNewFilesCron Servlet");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		//fetch the authorization code from the url.
		String code = request.getParameter("code");

		//if its a call back request then fetch the code to fetch Google Drive Files through API.
		if( code!=null ) {
			String authCode = code;

			//storing the received auth code to the session.
			request.getSession().setAttribute("driveAuthCode", authCode);

			//fetching GoogleCredential to pass it on to the Drive API.
			GoogleTokenResponse googleResponse = flow.newTokenRequest(authCode).setRedirectUri("http://www.gappsdemo-1380.appspot.com/cron/fetchnewfiles").execute(); 
			GoogleCredential googleCredential = new GoogleCredential().setFromTokenResponse(googleResponse);

			//Create a new authorized API client 
			Drive driveService = new Drive.Builder(httpTransport, jsonFactory, googleCredential).build(); 

			//fetch the list of all the files from the Drive.
			List<File> result = new ArrayList<File>();
			Files.List req = null;

			try {
				req = driveService.files().list();

				//Query to fetch only files not folders and those files which are owned by the users.
				FileList files = req.setQ("'root' in parents and trashed=false and mimeType != 'application/vnd.google-apps.folder' and 'me' in owners")
						.setFields("nextPageToken, files(id, name, createdTime)").execute();
				result.addAll(files.getFiles());          

				req.setPageToken(files.getNextPageToken());
				req.setOrderBy("createdTime desc");

				for(File f : result) {

					String fileDateStr = f.getCreatedTime().toString();

					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					Date currentDateTime = new Date();

					currentDateTime = dateFormat.parse(dateFormat.format(currentDateTime));

					fileDateStr = fileDateStr.replace("T", " ");
					fileDateStr = fileDateStr.replace("Z", "");

					Date fileDate = dateFormat.parse(fileDateStr);

					long fileCreationDifference = currentDateTime.getTime() - fileDate.getTime();
					long diffMinutes = fileCreationDifference / (60 * 1000) % 60;

					if( diffMinutes<=10 ) {
						Document newDoc = Document.newBuilder().setId(f.getId())
								.addField(Field.newBuilder().setName("fileId").setText(f.getId()))
								.addField(Field.newBuilder().setName("fileName").setText(f.getName())).build();

						//Add the Document instance to the Search Index
						SearchIndexManager.INSTANCE.indexDocument(Constant.FILES_INDEX_NAME, newDoc);
					}
				}
			}
			catch (Exception e)
			{
				System.out.println("An error occurred: " + e);
				req.setPageToken(null);
			}
		} else { 
			//get the authorization url
			String url = getAuthorizationUrl();

			//redirect response to the authorization url to get the auth code.
			response.sendRedirect(url);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.info("cron job called in doPost");
		doGet(request, response);
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
		String url = flow.newAuthorizationUrl().setRedirectUri("http://www.gappsdemo-1380.appspot.com/cron/fetchnewfiles").build();

		return url;
	}

}