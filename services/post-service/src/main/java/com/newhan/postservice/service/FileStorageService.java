package com.newhan.postservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${FILE_UPLOAD_DIR:./uploads/posts}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the upload directory.", ex);
        }
    }

    public String saveFile(MultipartFile file) {
        try {
            // 1. Get original name or default
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) originalName = "image.jpg";

            // 2. Clean the name (remove spaces and special chars)
            String safeName = originalName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

            // 3. New Format: Name_Timestamp.jpg (Readable!)
            String fileName = safeName + "_" + System.currentTimeMillis();
            
            // Ensure extension exists
            if (!fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".png")) {
                fileName += ".jpg";
            }

            // 4. Save
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5. Return path
            return "/images/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}