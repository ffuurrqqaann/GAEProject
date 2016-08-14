<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.appengine.api.datastore.Entity"%>
<%

String searchText = null;
//Read the searchText Request Parameter
if (request.getParameter("searchText") != null) {
	searchText = (String) request.getParameter("searchText");
	System.out.println("Search Text : " + searchText);
}
%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<title>Google Docs Listing</title>
	<meta name="generator" content="BBEdit 10.1" />
	
	<script type="text/javascript" src="https://code.jquery.com/jquery-3.1.0.min.js"></script>
	<script type="text/javascript" src="https://code.jquery.com/ui/1.12.0/jquery-ui.min.js"></script>
	<script src="js/jquery-ui-1.7.3.custom.min.js" type="text/javascript"></script>
	<script src="js/jquery.dataTables.min.js" type="text/javascript"></script>
	
	<link rel="stylesheet" href="css/redmond/jquery-ui-1.7.3.custom.css" />
	<link type="text/css" href="css/jquery.dataTables.css" rel="stylesheet" />
	
	<script type="text/javascript">
			$(document).ready(function() {
				
				//Click Handler for Search Button
				$('#searchBtn').click(function() {
                   	var searchText = $('#searchText').val();
                   	if (searchText.length == "") {
                   		alert("Please enter a search term");
                   		return;
                   	}
                   	document.forms['searchForm'].submit();
				});

				//This is the API call for retrieving the Search Records
				//It invokes the Contacts Directory Index Service
				var table_url = "/fileindexservice"
				var oTable = $('#result-table').dataTable( {
						"bJQueryUI": true,
						"bProcessing": true,
	       				"sAjaxSource": table_url+"?searchText=<%=searchText%>",
	       				"sAjaxDataProp": "results",
	       				"iDisplayLength": 50,
	       				"oSearch": {"sSearch":""},
	       				"oLanguage": {"sSearch":"Filter:"},
	       				"aoColumns": [
	       					{ "mData": "name" }
	       				],
	       				"aaSorting": [[ 0, "asc" ]]
				});
			});
			
		</script>
</head>
<body>
	
	<h1>File Directory</h1>
	    <!--  Search Form. -->
		<div id="basic">
			<form method="get" name="searchForm" id="searchForm">
				<input name="searchText" id="searchText" placeholder="Enter Search Term"></input>
				<input type="button" value="Search" id="searchBtn"></input>&nbsp;|&nbsp;
			</form>
		</div>
		
		<!-- Search Results Table -->
		<div id="basic">
			<table id="result-table">
				<thead>
					<tr>
						<th>File Name</th>
					</tr>
				</thead>
			</table>
		</div>
	
</body>
</html>
