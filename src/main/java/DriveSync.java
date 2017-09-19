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

    public static void main(String[] args) throws IOException {

        Drive driveService = SyncFunctions.getDriveService();

        try
        {
            SyncFunctions.uploadFile(driveService, "dog.jpg", "/desktop/");
        }

        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        return;

    }

}
