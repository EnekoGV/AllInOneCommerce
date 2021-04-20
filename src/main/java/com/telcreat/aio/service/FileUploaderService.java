package com.telcreat.aio.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploaderService {

    public String uploadUserPicture(MultipartFile file, int id, String directory){
        // check if file is empty
        if (file.isEmpty()) {
            return null; // Return null if there is no file
        }

        String rootImageDirectory = "./src/main/resources/static"; // Set Image storage directory
        //String userImageDirectory = "/user" + userId + "/"; // Set user's image directory

        // normalize the file path
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String[] nameParts = originalFileName.split("\\.");
        String fileName = "/img" + directory + "/" + id + "." + nameParts[nameParts.length-1];

        // save the file on the local file system
        try {
            Path path = Paths.get(rootImageDirectory + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName; // Return uploaded file complete path
    }
}
