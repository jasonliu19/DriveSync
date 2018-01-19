package coreapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtilities {
    public static boolean isValidPath(String path){
        File filePath = new File(path);
        if(filePath.isDirectory()){
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

    public String removeFileExtension(String fileName){
        int index = fileName.indexOf('.');
        if(index == -1)
            return fileName;

        return fileName.substring(0, index);
    }

    public static long getTotalDirSizeKB(String path){
        java.io.File folder = new java.io.File(path);

        long sizeInBytes = getTotalDirSizeHelper(path);
        return getTotalDirSizeHelper(path)/(1024);

        
    }
    
    private static long getTotalDirSizeHelper(String path){
        java.io.File folder = new java.io.File(path);

        //Iterate through files
        long totalSize = 0;
        for (java.io.File file : folder.listFiles())
        {
            if(file.isFile())
            {
                totalSize += file.length();
            }
            else if (file.isDirectory())
            {
                totalSize += getTotalDirSizeHelper(file.getPath()+"/");
            }
        }
        return totalSize;
    }
    
    public FileUtilities(){

    }
}
