package com.bmgf.service.impl;

import com.bmgf.DTO.FeedbackDTO;
import com.bmgf.dao.FeedbackRepository;
import com.bmgf.po.Feedback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Value("${storage.media.feedback.location}")
    private String uploadPath;

    public Feedback submitFeedback(FeedbackDTO feedbackDTO) throws IOException {
        Feedback feedback = new Feedback();
        feedback.setType(feedbackDTO.getType());
        feedback.setTitle(feedbackDTO.getTitle());
        feedback.setContent(feedbackDTO.getContent());
        feedback.setContact(feedbackDTO.getContact());
        feedback.setStatus("pending");
        feedback.setCreateTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());

        // 上传文件处理
        List<String> imageUrls = new ArrayList<>();
        if (feedbackDTO.getFiles() != null && !feedbackDTO.getFiles().isEmpty()) {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir); // 确保目录存在

            for (MultipartFile file : feedbackDTO.getFiles()) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String extension = getFileExtension(originalFilename);
                    String fileName = UUID.randomUUID() + extension;

                    Path targetPath = uploadDir.resolve(fileName);
                    file.transferTo(targetPath.toFile());

                    // 正确生成可访问路径
                    String accessUrl = "/uploads/feedback/" + fileName;
                    imageUrls.add(accessUrl);

                    log.info("反馈图片已上传: {}", accessUrl);
                }
            }
        }

        feedback.setImageUrls(imageUrls);
        return feedbackRepository.save(feedback);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex != -1 ? fileName.substring(dotIndex) : "";
    }

    public Page<Feedback> getAllFeedback(Pageable pageable) {
        return feedbackRepository.findAll(pageable);
    }

    public Page<Feedback> getFeedbackByStatus(String status, Pageable pageable) {
        return feedbackRepository.findByStatus(status, pageable);
    }

    public Feedback updateFeedbackStatus(String id, String status) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("反馈不存在"));

        feedback.setStatus(status);
        feedback.setUpdateTime(LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }

    public void deleteFeedback(String id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("反馈不存在"));

        if (feedback.getImageUrls() != null) {
            for (String imageUrl : feedback.getImageUrls()) {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadPath, fileName);
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    log.warn("删除反馈图片失败: {}", filePath);
                }
            }
        }

        feedbackRepository.deleteById(id);
    }

    public Map<String, Long> getFeedbackStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", feedbackRepository.count());
        stats.put("pending", feedbackRepository.countByStatus("pending"));
        stats.put("processing", feedbackRepository.countByStatus("processing"));
        stats.put("resolved", feedbackRepository.countByStatus("resolved"));
        return stats;
    }
}

