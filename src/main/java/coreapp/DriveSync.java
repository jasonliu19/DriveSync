package coreapp;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

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
            String response = " ";
            Scanner scanner = new Scanner(System.in);
            while(!response.equals("y") && !response.equals("n")){
                response = scanner.next() ;
            }

            if(response.equals("y")){
                
                driveService = DriveInitializer.getDriveService();
                DriveInitializer.updateRootDriveConstant();

                startSync(path, null);
            }
            else
                System.out.println("Aborted");

        }else{
            driveService = DriveInitializer.getDriveService();
            DriveInitializer.updateRootDriveConstant();
            startSync(path, null);
        }
    }
    public static void main(String[] args) throws IOException {

        boolean startGUI = false;
        String path;

        if(args.length == 0){
            System.out.println("Please enter a valid command:");
            System.out.println("'-g' -starts a GUI based uploader");
            System.out.println("   e.g. 'drivesync -g'");
            System.out.println("'pathToFolder' -uploads a folder without a GUI");
            System.out.println("   e.g. 'drivesync /home/user/Documents/folderToBeUploaded'");
            return;
        }

        for(String s : args){
            if(s.equals("-g")){
                System.out.println("Starting GUI");
                startGUI = true;
                break;
            }
            if(s.equals("-t")){
                System.out.println("Starting test");
                path = args[args.length-1];
                System.out.println("Size of " + path +" is: " +FileUtilities.getTotalDirSizeKB(path));
                return;
            }
        }

        path = args[args.length-1];
        
        System.out.println("Path: " + path);
        //Create drive service
        //Create and display the GUI
        if(startGUI) {
            driveService = DriveInitializer.getDriveService();
            DriveInitializer.updateRootDriveConstant();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new FrontEndGUI().setVisible(true);

                }
            });
        } else{
            startSyncFromShell(path);
        }
    }

}
