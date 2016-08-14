/**
 * This Java Servlet is the main interaction gateway for the UI. It is primarily invoked from the contacts.jsp page to 
 * search for Contacts. This Servlet interacts primarily with the App Engine Search API to handle the documents within the Index.
 * 
 * It also functions as a REST API
 * - /contactsdirectoryindexservice -> This will retrieve the documents in the index
 * - /contactsdirectoryindexservice?searchText=SOME_VALUE -> This will retrieve the documents in the index that match the search term
 * 
 * The API will provide JSON formatted responses
 */
package com.demo.gapps.server.servlet;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.search.Document;
import com.google.gson.Gson;
import com.demo.gapps.model.File;
import com.demo.gapps.search.SearchIndexManager;

@SuppressWarnings("serial")
public class FileIndexService extends HttpServlet {
	
	/**
	 * The GET Method is where the Search Magic happens. It extracts out the searchText request parameter and invokes the 
	 * SearchIndexManager class to retrieve the documents that match the searchText
	 */

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		//Extract out the search Text entered by the user on the contacts.jsp page
		String searchText = req.getParameter("searchText");
		try {
			//Check if the search Text is not empty
			if (!searchText.equals("null")) {
				System.out.println("search text is "+searchText);
				
				List<File> files = new ArrayList<File>();
				List<Document> documents = SearchIndexManager.INSTANCE.retrieveAllDocuments();
				
				
				for(Document document : documents) {
					String fileName = document.getOnlyField("fileName").getText();
					
					Boolean isFound = fileName.toLowerCase().contains(searchText.toLowerCase());
					
					if( isFound ) {
						File _File = new File();
						_File.setName(fileName);
						
						files.add(_File);
					}
				}
				
				res.getOutputStream().print("{\"results\":" +new Gson().toJson(files)+"}");
						
			} else {
				//if there is no search text found then show all the records in the search index.
				List<File> files = new ArrayList<File>();
				
				//retrieve all the search documents.
				List<Document> documents = SearchIndexManager.INSTANCE.retrieveAllDocuments();
				
				for(Document document : documents) {
					String fileName = document.getOnlyField("fileName").getText();
					
					File _File = new File();
					_File.setName(fileName);
					
					files.add(_File);
				}
				
				//Marshall the Collection into a JSON representation using the GSON Java library
				res.getOutputStream().print("{\"results\":" +new Gson().toJson(files)+"}");
			}
		} catch (Exception e) {
			res.getOutputStream().println(getFailureMessage());
		}
	}

	/**
	 * The POST method is currently not supported
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String status = "Not supported";
		res.getOutputStream().print(status);
	}

	/**
	 * The PUT method currently routes to the POST method
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String status = "Not supported";
		res.getOutputStream().print(status);
	}
	
	private String getFailureMessage() {
		return ("[{status:\"failed\"}]");
	}
}
