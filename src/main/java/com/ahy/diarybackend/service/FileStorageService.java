package com.ahy.diarybackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/diary-images}")
    private String uploadDir;

    // 파일 저장
    public String storeFile(MultipartFile file) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 확장자 추출
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // UUID로 고유한 파일명 생성
        String storedFileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(storedFileName);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return storedFileName;
    }

    // 파일 삭제
    public void deleteFile(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        Files.deleteIfExists(filePath);
    }

    // 파일 경로 반환
    public String getFilePath(String fileName) {
        return uploadDir + File.separator + fileName;
    }

    // 파일 존재 여부 확인
    public boolean fileExists(String fileName) {
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        return Files.exists(filePath);
    }

}
