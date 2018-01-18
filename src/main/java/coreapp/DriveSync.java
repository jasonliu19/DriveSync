package coreapp;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

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
        }

        path = args[args.length-1];

        //Create drive service
        driveService = DriveInitializer.getDriveService();
        DriveInitializer.updateRootDriveConstant();
        //Create and display the GUI
        if(startGUI) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new FrontEndGUI().setVisible(true);

                }
            });
        } else{
            startSync(path, null);
        }
    }

}
