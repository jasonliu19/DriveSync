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
    public static void main(String[] args) throws IOException {

        //Create drive service
        driveService = SyncFunctions.getDriveService();
        //Create and display the GUI
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrontEndGUI().setVisible(true);
            }
        });
        
    }

}
