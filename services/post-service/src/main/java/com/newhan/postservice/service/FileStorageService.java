package com.newhan.postservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${FILE_UPLOAD_DIR:/app/uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        // Create directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate unique filename (uuid + original extension)
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".") 
                ? originalName.substring(originalName.lastIndexOf(".")) 
                : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, file.getBytes());

        // Return the path that the Android app will use to download it
        return "/images/" + fileName;
    }
}