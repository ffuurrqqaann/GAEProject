package com.demo.gapps;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AddNewDocumentTest {

	private final LocalServiceTestHelper helper =
			new LocalServiceTestHelper(new LocalSearchServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void addDocumentTest() {
		
		System.out.println("addDocumentTest started.");
		
		Document newDoc = Document.newBuilder().setId("1")
				.addField(Field.newBuilder().setName("fileId").setText("testFileId1"))
				.addField(Field.newBuilder().setName("fileName").setText("testFileName1")).build();
		
		Document newDoc1 = Document.newBuilder().setId("2")
				.addField(Field.newBuilder().setName("fileId").setText("testFileId2"))
				.addField(Field.newBuilder().setName("fileName").setText("testFileName2")).build();
		
		IndexSpec indexSpec = IndexSpec.newBuilder().setName("TestFileIndex").build(); 
	    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
	    
	    try {
	    	//Put the Document in the Index. If the document is already existing, it will be overwritten
	        index.put(newDoc);
	        index.put(newDoc1);
	    } catch (PutException e) {
	        e.printStackTrace();
	    }
		
	    //fetching the count of documents in the search index.
		GetRequest request = GetRequest.newBuilder().build();
		int actualDocumentLength = index.getRange(request).getResults().size();
		
		// checking documents count in the backend.
		Assert.assertEquals(2, actualDocumentLength);
	}
	
	
	
	

}
