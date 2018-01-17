package coreapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtilities {
    public boolean isValidPath(String path){
        File filePath = new File(path);
        if(filePath.exists()){
            return true;
        }
        return false;
    }

    public String ensureEndSlash(String path){
        if(path.endsWith("/")){
            return path;
        } else {
            return path + "/";
        }
    }

    public String getMimeType(String path){
        try{
            return Files.probeContentType(Paths.get(path));
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public FileUtilities(){

    }
}
