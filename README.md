# Google Apps Engine Application

This Application allows a user to login through Gmail Account to sync the Google Drive Files with this application and able to search their files from their GDrive. The Application uses following key technologies.

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

1. The backend process(cron) is working perfectly good i.e., runs every 10 minutes. The only issue is that the due to some restrictions the url redirection is not working.
2. Another known issue in search API indexing is it does not support full text search the same case is with the google app engine datastore, thats why I have to handle the search functionality on application level that can be seen in the code.
