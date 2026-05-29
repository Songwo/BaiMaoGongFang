package com.bmgf.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 白帽工坊 · 靶场沙箱编排服务入口。
 * 职责：Docker 容器/Compose 编排、端口与子域扫描等安全工具；独立部署，自有 PostgreSQL 库。
 */
@SpringBootApplication
@EnableScheduling
public class SandboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(SandboxApplication.class, args);
    }
}
