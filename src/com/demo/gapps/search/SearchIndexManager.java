/**
 * This class handles all the interaction with Google AppEngine Search API. 
 * Currently, it has methods for:
 * 1. Index a document i.e. add a document to the existing index
 * 2. Retrieve a Document from the Index, provided its Document Id (In our case it is the user id)
 * 3. Remove a Document from the Index, provided its Document Id
 * 4. Retrieve a List of Documents from the Index that match the Search Term. 
 */
package com.demo.gapps.search;

import java.util.List;

import com.google.api.services.drive.model.File;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;
import com.demo.gapps.shared.Constant;

public enum SearchIndexManager {
	INSTANCE;

	/**
	 * This method receives the Google Drive File list and invokes the processFiles method.
	 */
	public void loadData(List<File> files) {
		try {
			processFiles(files);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * It will receive the list of Google Drive Files and Insert it as a Search Index Document.
	 * @param Google Drive Files list
	 * @throws Exception
	 */
	public void processFiles(List<File> files) throws Exception {

		for(File f : files) {
			Document newDoc = Document.newBuilder().setId(f.getId())
					.addField(Field.newBuilder().setName("fileId").setText(f.getId()))
					.addField(Field.newBuilder().setName("fileName").setText(f.getName())).build();

			//Add the Document instance to the Search Index
			SearchIndexManager.INSTANCE.indexDocument(Constant.FILES_INDEX_NAME, newDoc);
		}
	}
	
	/**
	 * This method is used to add a Document to a particular Index
	 * @param indexName This is the name of the Index to which the document is to be added. An index serves as a logical collection of documents
	 * @param document This is the Document instance that needs to be added to the Index
	 */
	public void indexDocument(String indexName, Document document) {
		//Setup the Index
	    IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build(); 
	    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
	    
	    try {
	    	//Put the Document in the Index. If the document is already existing, it will be overwritten
	        index.put(document);
	    } catch (PutException e) {
	        if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
	            // retry putting the document
	        }
	    }
	}

	/**
	 * This method is used to retrieve a particular Document from the Index
	 * @param documentId This is the key field that uniquely identifies a document in the collection i.e. the Index. In our case
	 * it is the user id
	 * @return An instance of the Document object from the Index.
	 */
	public Document retrieveDocument(String documentId) {
		//Setup the Index
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(Constant.FILES_INDEX_NAME).build(); 
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

		//Retrieve the Record from the Index
		return index.get(documentId);
	}
	
	/**
	 * This method is used to retrieve a list of documents from the Index that match the Search Term.
	 * @param searchText The search term to find matching documents. By default, if you do not use the Search Language Syntax,
	 * it will retrieve all the records that contain a partial or full text match for all attributes of a document
	 * @return A collection of Documents that were found
	 */
	public List<Document> retrieveAllDocuments() {
		//Setup the Index
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(Constant.FILES_INDEX_NAME).build(); 
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
		
		GetRequest request = GetRequest.newBuilder().build();
		
		return index.getRange(request).getResults();
	}
	
	/**
	 * This method is used to retrieve a list of documents from the Index that match the Search Term.
	 * @param searchText The search term to find matching documents. By default, if you do not use the Search Language Syntax,
	 * it will retrieve all the records that contain a partial or full text match for all attributes of a document
	 * @return A collection of Documents that were found
	 */
	public Results<ScoredDocument> retrieveDocuments(String searchText) {
		//Setup the Index
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(Constant.FILES_INDEX_NAME).build(); 
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
		
		//Retrieve the Records from the Index
		return index.search("fileName="+searchText);
	}
	
}