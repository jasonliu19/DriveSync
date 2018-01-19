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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SyncThread extends Thread {

    private StatusUpdateCallback parentClass;
    private String startingPath;
    private String startingFolderID;
    private FileUtilities fileUtilities;
    @Override
    public void run(){
        try {
            uploadFolder(startingPath, startingFolderID);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public SyncThread(StatusUpdateCallback parentClass, String startingPath, String startingFolderID){
        fileUtilities = new FileUtilities();
        this.parentClass = parentClass;
        this.startingFolderID = startingFolderID;
        this.startingPath = startingPath;
    }

    public File updateMainDriveFolder()throws IOException{
        File rootFolder;
        rootFolder = checkForFolder(Constants.ROOT_FOLDER_NAME, null);
        if (rootFolder == null)
            rootFolder = createFolder(Constants.ROOT_FOLDER_NAME, "root");
        return rootFolder;
    }

    /**
    * Searches for and returns a file describing the first folder matching
    * the given query, returns null if no folder is found
    * String name: The name of the folder being searched
    * @return the File describing the found folder, or null if no folder is found
    */
    public File checkForFolder(String name, String parentID) throws IOException
    {
        //Null signifies no parent
        String pageToken = null;
        if(parentID == null){
            parentID = "root";
        }
        do {
            String Q = "mimeType='application/vnd.google-apps.folder' and name = \""
                            + name + "\" and '" + parentID +"' in parents and trashed = false";
//            System.out.println("Name of folder: " + name +" Query: " + Q);
            FileList returnedFiles = DriveSync.driveService.files().list()
                    .setQ(Q)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
          for (File file : returnedFiles.getFiles()) {
//            System.out.printf("Found folder: %s (%s)\n",
//                file.getName(), file.getId());
            return file;
          }
          pageToken = returnedFiles.getNextPageToken();
        } while (pageToken != null);


        return null;
    }

    private File checkForFile(String filename, String path, String parentID) throws IOException
    {
        //Null signifies no parent
        String mimeType = fileUtilities.getMimeType(path+filename);
        String pageToken = null;
        if(parentID == null){
            parentID = "root";
        }
        do {
            String Q = "mimeType='"+ mimeType+ "' and name = \""
                    + filename + "\" and '" + parentID +"' in parents and trashed = false";
//            System.out.println("Query: " + Q);
            FileList returnedFiles = DriveSync.driveService.files().list()
                    .setQ(Q)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, md5Checksum)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : returnedFiles.getFiles()) {
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
    public File createFolder(String name, String parentID)
            throws IOException
    {
        File folderMetadata = new File();
        if (parentID != "root")
            folderMetadata.setParents(Collections.
                    singletonList(parentID));

        folderMetadata.setName(name);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
//        System.out.println ("Creating folder with name: " + name);
        return(DriveSync.driveService.files().create(folderMetadata)
            .setFields("id, parents")
            .execute());

    }

    public File getFolder(String name, String parentID) throws IOException{
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
    public void uploadFolder(String path, String thisFolderID)
    {
        //Convert path to java file
        java.io.File folder = new java.io.File(path);

        //Iterate through files
        for (java.io.File file : folder.listFiles())
        {
            if(file.isFile())
            {
//                System.out.print("File has name: " + file.getName());
                try {
                    long fileSize = file.length()/1024;
                    if(fileSize < Constants.LARGE_FILE_SIZE || Constants.ALLOW_LARGE_FILES)
                        updateFileContent(file.getName(), path, thisFolderID);
                    else
                        System.out.println(file.getName() + " exceeds max file size (" + fileSize/1024 +"MB)");
                } catch (IOException e) {
                    System.err.println("Could not update " + file.getName());
                    e.printStackTrace();
                }
            }
            else if (file.isDirectory() && Constants.RECURSIVELY_UPLOAD_SUBFOLDERS)
            {
                File nextFolder = null;
                try {
                    nextFolder = getFolder(file.getName(), thisFolderID);
                } catch (IOException e) {
                    System.err.println("Error retrieving data for the folder " + file.getName());
                    e.printStackTrace();
                }

//                System.out.println("Name of folder" + file.getPath()+ "\\");
                uploadFolder(file.getPath()+"/", nextFolder.getId());
            }
        }
    }

    private void updateFileContent(String filename, String path, String parentID) throws IOException{


        File currentDriveFile = checkForFile(filename, path, parentID);
        if(currentDriveFile == null){
            System.out.println("Uploading " + filename + " (" + FileUtilities.getTotalDirSizeKB(path) + "KB)");
            uploadFile(filename, path, parentID);
        } else{
            String combinedPath = path + filename;
            java.io.File filePath = new java.io.File(combinedPath);

            File metaData  = new File();
            metaData.setName(filename);

            String md5 = null;
            //Generate MD5 checksum for local file
            try {
                md5 = MD5Checksum.getMD5Checksum(combinedPath);
            } catch (Exception e){
                System.err.println("Failed to get md5checksum of local file");
            }

            if(!md5.equals(currentDriveFile.getMd5Checksum())){
                FileContent mediaContent = new FileContent(currentDriveFile.getMimeType(), filePath);

                File uploadResult = DriveSync.driveService.files().update(currentDriveFile.getId(), metaData, mediaContent)
                        .execute();
//            System.out.println("--Updated file with ID: " + uploadResult.getId());

                System.out.println("Updated " + filename);
            } else{
                System.out.println(filename + " is already up to date.");
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
    private void uploadFile(String filename, String path, String parentID) throws IOException
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

//        System.out.println(filename + " has type: " + mimeType);
        FileContent mediaContent = new FileContent(mimeType, filePath);
        //Attempt to upload file
        File file = DriveSync.driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, parents")
            .execute();
        if(parentClass != null) {
            synchronized (parentClass) {
                parentClass.updateStatus("Uploaded " + filename);
            }
        }
        System.out.println("Uploaded " + filename);

    }


}
