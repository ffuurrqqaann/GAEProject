# GappsDemo
This project has been developed for Gapps Oy as an Interview test

This Application allows a user to login through Gmail Account to sync the Google Drive Files with this application. The Application uses following key technologies.

Key technologies/frameworks:

1. Java
2. Google App Engine using sdk 1.9.38
3. Google App Engine Search API for Index Documents
4. Google Drive API v3
5. Jquery
6. Datatables
7. Junit for unit testing
8. OAuth2

The Application can be found @ http://gappsdemo-1380.appspot.com/

Project/Application Description:
The test cases can be found in the test folder of the application. A cron job has been setup which is described in cron.xml that tries to fetch new files after every 10 minutes from the user google drive. A user first needs to login using gmail account, after OAuth Authentication the Google Drive Api fetch the user drive's files and import it into the Google App Engine and then the user can search its desire files through the user Interface of the application.

Things not considered to be Implemented:
The application right now works only for a single user so the user management has not been implemented for now.

Known issues:
	The backend process(cron) is working perfectly good i.e., runs every 10 minutes. The only issue is that the due to some restrictions the url redirection is not working. I have followed two approaches in which first one is implemented.

	1. The way I am fetching the drive files while running the application , but it requires url redirection that is not happening in cron.

	2. I am using the tutorial i.e., [https://developers.google.com/drive/v3/web/quickstart/java] in which a credential file needs to be stored in the directory and its a Google App Engine restriction that prevents an application to create new files and thus causing exception.

	So right now backend process is working but new files are not getting fetched due to the restrictions stated above.

	
	Another known issue in search API indexing is it does not support full text search the same case is with the google app engine datastore, thats why I have to handle the search functionality on application level that can be seen in the code.