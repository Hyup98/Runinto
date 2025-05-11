package com.runinto.util;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final String UPLOAD_DIR = "C:/Users/강동협/Desktop/Runinto/backend/uploads/images";
    private static final String WEB_ACCESS_PATH = "/images/";

    public String saveImage(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String newFilename = UUID.randomUUID() + "_" + originalFilename;

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());

        return WEB_ACCESS_PATH + newFilename;
    }
}
