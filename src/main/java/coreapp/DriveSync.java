package coreapp;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DriveSync {
    public static Drive driveService;

    public static void startSync(String path, StatusUpdateCallback parentObject){
        SyncThread syncThread;
        FileUtilities fileUtilities = new FileUtilities();
        try
        {
            if(!fileUtilities.isValidPath(path)){
                throw new IOException("Please enter a valid path.");
            }


            path = fileUtilities.ensureEndSlash(path);
            
            driveService = DriveInitializer.getDriveService();
            
            Constants.ROOT_FOLDER_NAME = Files.getNameWithoutExtension(path) + " (DS)";
            
            DriveInitializer.updateRootDriveConstant();
            
            syncThread = new SyncThread(parentObject, path, Constants.ROOT_FOLDER_ID);
            syncThread.start();

        }
        catch (IOException e)
        {
            if(parentObject!= null)
                parentObject.updateStatus(e.getMessage());
            System.out.println(e.getMessage());
        }
    }


    private static boolean verifyWithUser(){
        String response = " ";
        Scanner scanner = new Scanner(System.in);
        while(!response.equals("y") && !response.equals("n")){
            response = scanner.next() ;
        }
        if(response.equals("y"))
            return true;
        else
            return false;

    }
    
    private static void startSyncFromShell(String path){
        FileUtilities fileUtilities = new FileUtilities();
        try
        {
            if(!fileUtilities.isValidPath(path)){
                throw new IOException("Please enter a valid path.");
            }


            path = fileUtilities.ensureEndSlash(path);

        } catch(IOException e){
            e.printStackTrace();
            System.exit(2);
        }
        
        long totalSizeOfData = FileUtilities.getTotalDirSizeKB(path);
        if(totalSizeOfData > Constants.DATASIZE_WARNING_AMOUNT){
            System.out.println(path + " has " +totalSizeOfData/1024 + "MB of data. Proceed? y/n");
            
            if(verifyWithUser())
                startSync(path, null);
            else
                System.out.println("Aborted");

        }else{
            startSync(path, null);
        }
    }
    public static void main(String[] args) throws IOException {

        boolean startGUI = false;
        String path;

        if(args.length == 0){
            System.out.println("Please enter a valid command:");
            System.out.println("'-g' -starts a GUI based uploader");
            System.out.println("   e.g. ./drivesync -g");
            System.out.println("lastargument -uploads this folder without a GUI");
            System.out.println("   e.g. ./drivesync '/home/user/Documents/folderToBeUploaded'");
            System.out.println("'-d' -Prevents subfolders from being uploaded");
            System.out.println("   e.g. ./drivesync -d '/home/user/Documents/folderToBeUploaded'");
            System.out.println("'-l' -Allows large(>25MB) files to be uploaded");
            System.out.println("   e.g. ./drivesync -l '/home/user/Documents/folderToBeUploaded'");
            System.out.println("'-s' -Run using previous path");
            System.out.println("   e.g. ./drivesync -s '/home/user/Documents/folderToBeUploaded'");                 
            return;
        }

        for(String s : args){
            if(s.equals("-d")){
                Constants.RECURSIVELY_UPLOAD_SUBFOLDERS = false;
            }
            if(s.equals("-l")){
                Constants.ALLOW_LARGE_FILES = true;
            }
            if(s.equals("-g")){
                System.out.println("Starting GUI");
                startGUI = true;
            }
            if(s.equals("-s")){
                System.out.println("Starting GUI");
                startGUI = true;
            }
            if(s.equals("-t")){
                path = args[args.length-1];
                System.out.println(Files.getNameWithoutExtension(path));
               
                return;
            }
        }
        
        
        //Create drive service
        //Create and display the GUI
        if(startGUI) {

            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new FrontEndGUI().setVisible(true);

                }
            });
        } else{
            if(Constants.USE_LAST_PATH){
                BufferedReader reader = new BufferedReader(new FileReader(Constants.SAVE_PATH));
                path = reader.readLine();
                reader.close();
                System.out.println(path + " will be backed up. Proceed? y/n");
                if(!verifyWithUser()){
                    System.out.println("Aborted");
                    System.exit(0);
                }
                
            }else{
                path = args[args.length-1];
                BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.SAVE_PATH));
                writer.write(path);
     
                writer.close();
            }
            startSyncFromShell(path);
        }
    }

}
