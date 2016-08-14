package com.demo.gapps;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.demo.gapps.model.File;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class FileNotFoundTest {

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

	// Run this test twice to prove we're not leaking any state across tests.
	@Test
	public void searchFilesNotFoundTest() {
		
		System.out.println("searchFilesNotFoundTest started.");
		
		//searching for all txt files.
		String searchText = ".txt";
		
		Document newDoc = Document.newBuilder().setId("1")
				.addField(Field.newBuilder().setName("fileId").setText("testFileId1"))
				.addField(Field.newBuilder().setName("fileName").setText("testFileName1.jpg")).build();
		
		Document newDoc1 = Document.newBuilder().setId("2")
				.addField(Field.newBuilder().setName("fileId").setText("testFileId2"))
				.addField(Field.newBuilder().setName("fileName").setText("testFileName2.jpg")).build();
		
		Document newDoc2 = Document.newBuilder().setId("3")
				.addField(Field.newBuilder().setName("fileId").setText("testFileId3"))
				.addField(Field.newBuilder().setName("fileName").setText("testFileName3.jpg")).build();
		
		Document newDoc3 = Document.newBuilder().setId("4")
				.addField(Field.newBuilder().setName("fileId").setText("testFileId4"))
				.addField(Field.newBuilder().setName("fileName").setText("testFileName4.pdf")).build();
		
		Document newDoc4 = Document.newBuilder().setId("5")
				.addField(Field.newBuilder().setName("fileId").setText("testFileId5"))
				.addField(Field.newBuilder().setName("fileName").setText("testFileName5.pdf")).build();
		
		Document newDoc5 = Document.newBuilder().setId("6")
				.addField(Field.newBuilder().setName("fileId").setText("testFileId6"))
				.addField(Field.newBuilder().setName("fileName").setText("testFileName6.pdf")).build();
		
		IndexSpec indexSpec = IndexSpec.newBuilder().setName("TestFileIndex").build(); 
	    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
	    
	    try {
	    	//Put the Document in the Index. If the document is already existing, it will be overwritten
	        index.put(newDoc);
	        index.put(newDoc1);
	        index.put(newDoc2);
	        index.put(newDoc3);
	        index.put(newDoc4);
	        index.put(newDoc5);
	    } catch (PutException e) {
	        e.printStackTrace();
	    }
		
		GetRequest request = GetRequest.newBuilder().build();
		List<Document> documents = index.getRange(request).getResults();
		
		List<File> files = new ArrayList<File>();
		
		for(Document document : documents) {
			String fileName = document.getOnlyField("fileName").getText();
			
			Boolean isFound = fileName.toLowerCase().contains(searchText.toLowerCase());
						
			if( isFound ) {
				File _File = new File();
				_File.setName(fileName);
				
				files.add(_File);
			}
		}
		
		//no txt file check.
		Assert.assertEquals(0, files.size());
	}
	
}