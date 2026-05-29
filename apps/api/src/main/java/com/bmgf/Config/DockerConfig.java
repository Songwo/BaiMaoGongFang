package com.bmgf.Config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class DockerConfig {

    @Value("${docker.host:}")
    private String dockerHost;

    @Bean
    public DockerClient dockerClient() {
        // 如果没有配置 docker.host，就根据当前操作系统自动判断
        if (dockerHost == null || dockerHost.isBlank()) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                dockerHost = "npipe:////./pipe/docker_engine"; // Windows
            } else {
                dockerHost = "unix:///var/run/docker.sock";    // Linux / Mac
            }
        }

        // 构建配置
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }
}
