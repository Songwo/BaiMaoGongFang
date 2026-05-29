package com.bmgf;

import com.github.dockerjava.api.DockerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@SpringBootApplication
@EnableMongoAuditing
@Slf4j
@EnableScheduling
public class bmgfApplication {

    public static void main(String[] args) {
        SpringApplication.run(bmgfApplication.class, args);
    }

    @Bean
    public CommandLineRunner initStorageDirs(
            @Value("${storage.media.video.location}") String videoPath,
            @Value("${storage.media.avatar.location}") String avatarPath,
            @Value("${storage.media.post.location}") String postPath,
            @Value("${storage.media.feedback.location}") String feedbackPath){

        return args -> {
            Path videoDir = Paths.get(videoPath).toAbsolutePath().normalize();
            Path avatarDir = Paths.get(avatarPath).toAbsolutePath().normalize();
            Path postDir = Paths.get(postPath).toAbsolutePath().normalize();
            Path feedBackDir = Paths.get(feedbackPath).toAbsolutePath().normalize();

            try {
                Files.createDirectories(videoDir);
                Files.createDirectories(avatarDir);
                Files.createDirectories(postDir);
                Files.createDirectories(feedBackDir);
                log.info("视频存储目录已初始化: {}", videoDir);
                log.info("头像存储目录已初始化: {}", avatarDir);
                log.info("文章封面存储目录已初始化: {}", postDir);
                log.info("意见存储目录已初始化: {}", feedBackDir);
            } catch (IOException e) {
                log.error("目录创建失败: {}", e.getMessage());
                throw new RuntimeException("存储目录初始化失败", e);
            }
        };
    }

    @Bean
    public CommandLineRunner dockerTest(DockerClient dockerClient) {
        return args -> {
            try {
                dockerClient.pingCmd().exec();
                log.info("Docker 连接成功！");
                log.info("Docker 版本: {}", dockerClient.versionCmd().exec().getVersion());
            } catch (Exception e) {
                log.error("Docker 连接失败: {}", e.getMessage());
                throw new RuntimeException("Docker 连接测试失败", e);
            }
        };
    }
}