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

    private static File checkForFolder(String name) throws IOException
    {
        String pageToken = null;
        System.out.println("mimeType='application/vnd.google-apps.folder' and name contains '" + name + "'");
        do {
          FileList result = DriveSync.driveService.files().list()
              .setQ("mimeType='application/vnd.google-apps.folder' and name contains '" + name + "'")
              .setSpaces("drive")
              .setFields("nextPageToken, files(id, name)")
              .setPageToken(pageToken)
              .execute();
          for (File file : result.getFiles()) {
            System.out.printf("Found folder: %s (%s)\n",
                file.getName(), file.getId());
            return file;
          }
          pageToken = result.getNextPageToken();
        } while (pageToken != null);
        
        
        return null;
    }
    
    
    public static File createFolder(String name, String parent) throws IOException
    {
        //If no folder exists
        File folderMetadata = new File();
        if (parent == "root")
            ;
        else if (parent == null)
        {
            File defaultParent = checkForFolder("DriveSync Test");
            folderMetadata.setParents(Collections.singletonList(defaultParent.getId()));
        }
        else
        {
            File newParent = checkForFolder(parent);
            folderMetadata.setParents(Collections.singletonList(newParent.getId()));
        }
        folderMetadata.setName(name);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        System.out.println ("Creating folder with name: " + name);
        return(DriveSync.driveService.files().create(folderMetadata)
            .setFields("id")
            .execute());
        
    }
    
    public static void uploadFolder(String path, String folderName) throws IOException
    {
        //Convert path to java file
        java.io.File folder = new java.io.File(path);

        //Iterate through files
        for (java.io.File file : folder.listFiles())
        {
            if(file.isFile())
            {
                uploadFile(DriveSync.driveService, file.getName(), path, folderName);
            }
            else if (file.isDirectory())
            {
                File newFolder = checkForFolder(file.getName());
                if (newFolder == null)
                {
                    createFolder(file.getName(), folderName);
                }
                    
//                System.out.println("Name of folder" + file.getPath()+ "\\");
                uploadFolder(file.getPath() + "\\", file.getName());
            }
        }
    }
    
    
    private static void uploadFile(Drive driveService, String filename, String path, String folderName) throws IOException
    {
        //See if drive already has folder, 
        File uploadFolder;
        
        //If default folder
        if (folderName == null)
        {
            uploadFolder = checkForFolder("DriveSync Test");
            if (uploadFolder == null)
                uploadFolder = createFolder("DriveSync Test", "root");
        }
        else
        {
            uploadFolder = checkForFolder(folderName);
            System.out.println("Folder:" + folderName + "has id: " + uploadFolder.getId());
        }
        
        
        File fileMetadata = new File();
        //Set file name
        fileMetadata.setName(filename);
        
        //Put file in folder
        fileMetadata.setParents(Collections.singletonList(uploadFolder.getId()));
        //Create path for the document to be uploaded
        String combinedpath =  path + filename;
        java.io.File filePath = new java.io.File(combinedpath);
        
        //Set file path and MIME type
        FileContent mediaContent = new FileContent("application/msword", filePath);
        //Attempt to upload file
        File file = driveService.files().create(fileMetadata, mediaContent)
            .setFields("id")
            .execute();
        System.out.println("Uploaded file with ID: " + file.getId());
        return;

    }


}
