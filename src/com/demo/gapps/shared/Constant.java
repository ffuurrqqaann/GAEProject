package com.demo.gapps.shared;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import com.google.api.client.extensions.appengine.http.urlfetch.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public interface Constant {

	final static Level LOG_LEVEL = Level.FINE;

	/* session attributes */
	final static String TARGET_URI = "TargetUri";

	final static String AUTH_USER_ID = "UserEmail";

	final static String AUTH_USER_EMAIL = "UserEmail";

	final static String AUTH_USER_NICKNAME = "UserNickname";

	final static String GOOG_CREDENTIAL_STORE = "GoogleCredentialStore";

	final static String GOOG_CREDENTIAL = "GoogleCredentialL";

	/* end of session attributes */

	/* Application name  */
	final static String APPLICATION_NAME = "Gapps Demo";
	
	/* Credentials file location  */
	final static String AUTH_RESOURCE_LOC = "/client_secrets.json";

	/* Google API Manager credentials client id */
	final static String GAPPS_DEMO_CLIENT_ID = "555767803079-q65549ooao1ktcof57pqg2s29r74uuem.apps.googleusercontent.com";

	/* Google API Manager credentials client secret */
	final static String GAPPS_DEMO_CLIENT_SECRET = "BlUHbDy21xLdkv4hn68zFXDk";

	/* Application redirect URI */
	final static String GAPPS_DEMO_REDIRECT_URI = "http://www.gappsdemo-1380.appspot.com/html/listDriveFiles.jsp";

	/* Application redirect URI for local use */
	final static String GAPPS_DEMO_REDIRECT_URI_LOCAL = "http://localhost:8888/html/listDriveFiles.jsp";

	/* Google API Manager credentials client id  for local use */
	final static String GAPPS_DEMO_CLIENT_ID_LOCAL = "1065186802923-9ocv62cij0nupqrj12rm2uotdisimvv0.apps.googleusercontent.com";

	/* Google API Manager credentials client secret for local use */
	final static String GAPPS_DEMO_CLIENT_SECRET_LOCAL = "aMIhFmuGk0MSJWOJG-i5khnP";

	final static String GAPPS_DEMO_SERVICE_ACCOUNT_ID = "gappsdemo-1380@appspot.gserviceaccount.com";

	/* FILES_INDEX_NAME represents the name of the Index in the Google Search Index for the application. */
	public static final String FILES_INDEX_NAME= "FilesIndex";

	/* Use for local testing */
	final static String OATH_CALLBACK_LOCAL = "http://localhost:8888/authSub";

	/* oauth call back for local use */
	final static String OATH_CALLBACK = "http://localhost:8888/authSub";

	/* oauth call back for live use */
	final static String OATH_CALLBACK_LIVE = "https://gappsdemo-1380.appspot.com/authSub";
	
	final static List<String> SCOPES = Arrays.asList(
			"https://mail.google.com/");

	/* static variable for HTTP_TRANSPORT */
	final static HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();

	/* static variable for JsonFactory */
	final static JsonFactory JSON_FACTORY = new JacksonFactory();

}