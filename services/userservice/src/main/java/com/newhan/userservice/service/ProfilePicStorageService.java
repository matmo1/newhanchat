package com.newhan.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ProfilePicStorageService {

    // Pulls the directory from application.properties or Docker environment
    @Value("${file.upload.dir:/app/uploads/users}") 
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            // Ensure the directory exists
            Files.createDirectories(Paths.get(uploadDir));
            
            // Create a unique file name
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetLocation = Paths.get(uploadDir).resolve(fileName);
            
            // Save the file
            Files.copy(file.getInputStream(), targetLocation);
            
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}