package coreapp;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.http.*;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SyncFunctions {
    /** Application name. */
    private static final String APPLICATION_NAME =
        "DriveSync";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/drive-java-drivesync");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;
    
    private static String ROOT_FOLDER_ID;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        int attempts = 0;

        //Deletes saved credentials on fail
        while (attempts < 3){
            try{
                InputStream in =
                        SyncFunctions.class.getResourceAsStream("/client_secret.json");
                GoogleClientSecrets clientSecrets =
                        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

                // Build flow and trigger user authorization request.
                GoogleAuthorizationCodeFlow flow =
                        new GoogleAuthorizationCodeFlow.Builder(
                                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                                .setDataStoreFactory(DATA_STORE_FACTORY)
                                .setAccessType("offline")
                                .build();
                Credential credential = new AuthorizationCodeInstalledApp(
                        flow, new LocalServerReceiver()).authorize("user");
                System.out.println(
                        "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
                return credential;
            }catch (IOException e){
                attempts++;
                DATA_STORE_DIR.delete();
                if(attempts >= 3){
                    throw e;
                }
            }
        }
        //Never called
        return null;
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static File updateMainDriveFolder()throws IOException{
        File rootFolder;
        rootFolder = checkForFolder("DriveSync Test", null);
        if (rootFolder == null)
            rootFolder = createFolder("DriveSync Test", "root");
        return rootFolder;
    }
    
    /**
    * Searches for and returns a file describing the first folder matching 
    * the given query, returns null if no folder is found
    * String name: The name of the folder being searched
    * @return the File describing the found folder, or null if no folder is found
    */
    public static File checkForFolder(String name, String parentID) throws IOException
    {
        //Null signifies no parent
        String pageToken = null;
        if(parentID == null){
            parentID = "root";
        }
        do {
            String Q = "mimeType='application/vnd.google-apps.folder' and name = '" 
                            + name + "' and '" + parentID +"' in parents and trashed = false";
//            System.out.println("Name of folder: " + name +" Query: " + Q);
            FileList returnedFiles = DriveSync.driveService.files().list()
                    .setQ(Q)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
          for (File file : returnedFiles.getFiles()) {
            System.out.printf("Found folder: %s (%s)\n",
                file.getName(), file.getId());
            return file;
          }
          pageToken = returnedFiles.getNextPageToken();
        } while (pageToken != null);
        
        
        return null;
    }
    
    /*
    * Creates folder with name name and parent parent
    * @name: name of the folder to be created
    * @parentID: id of the parent folder of the folder to be created
    * @return the File with information about the created folder
    */
    public static File createFolder(String name, String parentID) 
            throws IOException
    {
        File folderMetadata = new File();
        if (parentID != "root")
            folderMetadata.setParents(Collections.
                    singletonList(parentID));
            //            ;//Don't assign parent
//        else if (parent == null)
//        {
//            File defaultParent = checkForFolder("DriveSync Test");
//            folderMetadata.setParents(Collections.
//                    singletonList(defaultParent.getId()));
//        }
//        else
//        {
//            File newParent = checkForFolder(parent);
//            folderMetadata.setParents(Collections.
//                    singletonList(newParent.getId()));
//        }

        folderMetadata.setName(name);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        System.out.println ("Creating folder with name: " + name);
        return(DriveSync.driveService.files().create(folderMetadata)
            .setFields("id, parents")
            .execute());
        
    }
    
    public static File getFolder(String name, String parentID) throws IOException{
        File returnValue = checkForFolder(name, parentID);
        
        if(returnValue == null){
            returnValue = createFolder(name, parentID);
        }
        return returnValue;
    }
    /*
    * Uploads all the contents of a folder on the local machine
    * @path: Full path of the folder with a "/" at the end
    * @thisFolderID: ID of the folder to be uploaded
    */
    public static void uploadFolder(String path, String thisFolderID) 
            throws IOException
    {
        //Convert path to java file
        java.io.File folder = new java.io.File(path);

        //Iterate through files
        for (java.io.File file : folder.listFiles())
        {
            if(file.isFile())
            {
                System.out.print("File has name: " + file.getName());
                uploadFile(file.getName(), path, thisFolderID);
            }
            else if (file.isDirectory())
            {
                updateMainDriveFolder();
                File nextFolder = getFolder(file.getName(), thisFolderID);
                    
//                System.out.println("Name of folder" + file.getPath()+ "\\");
                uploadFolder(file.getPath()+"/", nextFolder.getId());
            }
        }
    }


    /*
    * Uploads a single file to Drive (Max ~10mb)
    * @filename: Name of the file (including extension) to be uploaded
    * @path: Full path of the folder containing the file (terminating in "/"
    * @parentID: Target folder in Drive i.e. the folder were the file 
    * will be placed in drive
    */
    private static void uploadFile(String filename, String path, String parentID) throws IOException
    {
        FileUtilities fileUtilities = new FileUtilities();
        
        
        File fileMetadata = new File();

        fileMetadata.setName(filename);

        fileMetadata.setParents(Collections.singletonList(parentID));
        //Create path for the document to be uploaded
        String combinedpath =  path + filename;
        java.io.File filePath = new java.io.File(combinedpath);
        
        //Set file path and MIME type
        String mimeType = fileUtilities.getMimeType(combinedpath);
        if(mimeType == null) //Do not upload
            return;

        System.out.println(filename + " has type: " + mimeType);
        FileContent mediaContent = new FileContent(mimeType, filePath);
        //Attempt to upload file
        File file = DriveSync.driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, parents")
            .execute();
        System.out.println("Uploaded file with ID: " + file.getId());

    }


}
