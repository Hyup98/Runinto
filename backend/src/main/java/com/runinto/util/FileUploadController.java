package com.runinto.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    // 업로드 디렉토리 경로 (절대 경로)
    private static final String UPLOAD_DIR = "C:\\Users\\강동협\\Desktop\\Runinto\\backend\\uploads\\images";
    // 웹 접근 URL 경로 (WebConfig의 uploadPath와 일치하도록 시작 부분 수정)
    private static final String WEB_ACCESS_PATH = "/images/";

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String newFilename = UUID.randomUUID() + "_" + originalFilename;
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());

        // WebConfig에서 설정한 경로와 일치하는 URL 반환
        String fileUrl = WEB_ACCESS_PATH + newFilename;
        return ResponseEntity.ok(fileUrl);
    }

}
