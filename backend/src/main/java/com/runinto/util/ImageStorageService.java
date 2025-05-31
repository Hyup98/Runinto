package com.runinto.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@ConfigurationProperties(prefix = "user") // 클래스 레벨에 @ConfigurationProperties 추가
public class ImageStorageService {

    private static final String UPLOAD_DIR = "C:/Users/강동협/Desktop/Runinto/backend/uploads/images";
    private static final String WEB_ACCESS_PATH = "/images/";

    private String defaultProfile; // application.yml의 user.default-profile과 매핑될 필드

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public String getDefaultProfile() { // 필요하다면 getter도 유지할 수 있습니다.
        return this.defaultProfile;
    }

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            if (this.defaultProfile == null || this.defaultProfile.isEmpty()) {
                throw new IllegalStateException("Default profile path is not configured.");
            }
            return this.defaultProfile;
        }

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