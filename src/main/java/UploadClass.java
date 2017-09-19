//package coreapp;
//
//
//public class UploadClass extends DriveSync {
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





