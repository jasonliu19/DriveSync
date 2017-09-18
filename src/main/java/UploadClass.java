//package coreapp;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.util.store.FileDataStoreFactory;
//
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.*;
//import com.google.api.services.drive.Drive;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.Arrays;
//import java.util.List;
//
//public class UploadClass {
//
//    public static void uploadFile(Drive driveService)
//    {
//        File fileMetadata = new File();
//        fileMetadata.setName("photo.jpg");
//        java.io.File filePath = new java.io.File("photo.jpg");
//        FileContent mediaContent = new FileContent("image/jpeg", filePath);
//        File file = driveService.files().create(fileMetadata, mediaContent)
//            .setFields("id")
//            .execute();
//        System.out.println("File ID: " + file.getId());
//    }
//
//}





