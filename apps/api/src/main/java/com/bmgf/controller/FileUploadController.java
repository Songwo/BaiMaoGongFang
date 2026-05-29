package com.bmgf.controller;

import com.bmgf.po.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    @Value("${storage.media.post.location}")
    private String uploadPath;

    @PostMapping("/post-cover")
    public Result uploadPostCover(@RequestParam("file") MultipartFile file,
                                  @RequestHeader("Authorization") String authHeader,
                                  HttpServletRequest request) {
        // 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 校验文件类型
        if (!file.getContentType().startsWith("image/")) {
            return Result.error("仅支持图片文件");
        }

        // 校验文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }

        try {
            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 生成新的文件名
            String fileName = UUID.randomUUID().toString() + extension;

            // 创建完整的目标路径
            Path uploadDir = Paths.get(uploadPath);

            // 确保目录存在
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 创建完整的文件路径并保存文件
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 返回文件访问路径
            String fileUrl = "posts/" + fileName;
            return Result.success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}