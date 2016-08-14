//This is just a testing class for development environment not for live.
package com.demo.gapps.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.demo.gapps.model.File;
import com.demo.gapps.search.SearchIndexManager;
import com.demo.gapps.shared.Constant;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;

@SuppressWarnings("serial")
public class DeleteSearchDocuments extends HttpServlet {

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY =
			JacksonFactory.getDefaultInstance();

	private final static Logger LOGGER = Logger.getLogger(FetchNewFilesCron.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();

		LOGGER.setLevel(Constant.LOG_LEVEL);
		LOGGER.info("Initializing FetchNewFilesCron Servlet");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.info("cron job called in doGet");
		
		List<Document> documents = SearchIndexManager.INSTANCE.retrieveAllDocuments();
		
		for(Document document : documents) {
			String id = document.getId();
			deleteDocumentFromIndex(id);
		}
		
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.info("cron job called in doPost");
		doGet(request, response);
	}
	
	public void deleteDocumentFromIndex(String documentId) {
		//Setup the Index
		IndexSpec indexSpec = IndexSpec.newBuilder().setName("FilesIndex").build(); 
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
		
		//Delete the Records from the Index
		index.delete(documentId);
	}
	
}