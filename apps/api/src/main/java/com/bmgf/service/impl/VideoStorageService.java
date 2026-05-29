package com.bmgf.service.impl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class VideoStorageService {
        private final Path uploadLocation;
        public VideoStorageService(@Value("${storage.media.video.location}") String uploadPath) {
            this.uploadLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
            try {
                Files.createDirectories(this.uploadLocation); // 关键创建目录代码
            } catch (IOException e) {
                throw new RuntimeException("无法创建视频存储目录", e);
            }
        }
    public String storeVideo(MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID() + fileExtension;
            Path targetLocation = this.uploadLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation);
            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("视频存储失败: " + e.getMessage(), e);
        }
    }
}