package com.bmgf.service.impl;

import com.bmgf.po.ContainerInstance;
import com.bmgf.service.MySQLService;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.api.DockerClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
public class ContainerService {
    private final DockerClient dockerClient;
    private final MongoTemplate mongoTemplate;
    private final MySQLService mySQLService;

    // 设置宿主机的 IP 地址，确保与 MySQL 服务连接
    private static final String MYSQL_HOST = "172.24.22.213"; // 你的宿主机 IP 地址
    private static final String MYSQL_USER = "root";
//    private static final String MYSQL_PASSWORD = "O7VeFozvbnWH79dy";
    private static final String MYSQL_PASSWORD = "123456";
    // 确保镜像存在，不存在则拉取镜像
    private void ensureImageExists(String imageName) {
        try {
            // 检查镜像是否已经存在
            List<Image> images = dockerClient.listImagesCmd()
                    .withImageNameFilter(imageName)
                    .exec();
            // 如果镜像不存在，则拉取镜像
            if (images.isEmpty()) {
                dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();
                System.out.println("镜像 " + imageName + " 拉取成功");
            } else {
                System.out.println("镜像 " + imageName + " 已经存在");
            }
        } catch (Exception e) {
            throw new RuntimeException("拉取镜像失败: " + e.getMessage(), e);
        }
    }
    public ContainerService(DockerClient dockerClient, MongoTemplate mongoTemplate, MySQLService mySQLService) {
        this.dockerClient = dockerClient;
        this.mongoTemplate = mongoTemplate;
        this.mySQLService = mySQLService;
    }
    // 创建自定义 Docker 网络（如果不存在）
    private void createNetworkIfNotExists(String networkName) {
        boolean exists = dockerClient.listNetworksCmd()
                .exec()
                .stream()
                .anyMatch(n -> n.getName().equals(networkName));
        if (!exists) {
            dockerClient.createNetworkCmd()
                    .withName(networkName)
                    .withDriver("bridge")  // 使用 bridge 网络驱动，保证容器间通信
                    .exec();
        }
    }
    // 创建共享环境
    public ContainerInstance createSharedEnvironment(String userId, String vulnType, int durationMinutes) {
        try {
            String networkName = "shared-net-" + vulnType.replaceAll("[^a-zA-Z0-9]", "_");
            createNetworkIfNotExists(networkName);

            String dbName = "user_" + userId + "_" + vulnType.replaceAll("[^a-zA-Z0-9]", "_");
            mySQLService.createDatabaseIfNotExists(dbName);

            String imageName = vulnType;
            String containerName = vulnType.replaceAll("[^a-zA-Z0-9_.-]", "_");

            // 确保镜像存在
            ensureImageExists(imageName);

            // 检查容器是否已存在
            List<Container> containers = dockerClient.listContainersCmd()
                    .withNameFilter(List.of("/" + containerName))
                    .withShowAll(true)
                    .exec();

            String containerId;
            Integer hostPort;

            if (!containers.isEmpty()) {
                Container container = containers.get(0);
                if (!container.getStatus().toLowerCase().contains("up")) {
                    dockerClient.startContainerCmd(container.getId()).exec();
                }
                containerId = container.getId();
                hostPort = 3000; // 默认端口
                for (ContainerPort port : container.getPorts()) {
                    if (port.getPrivatePort() == 3000 && port.getPublicPort() != null) {
                        hostPort = port.getPublicPort();
                        break;
                    }
                }
            } else {
                ExposedPort exposedPort = ExposedPort.tcp(3000);
                Ports portBindings = new Ports();

                // 判断是否为 sql 类型镜像，固定端口 32801
                if (imageName.toLowerCase().contains("sql")) {
                    hostPort = 32801;
                    portBindings.bind(exposedPort, Ports.Binding.bindPort(hostPort));
                } else {
                    hostPort = 3000;
                    portBindings.bind(exposedPort, Ports.Binding.empty());
                }

                CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                        .withName(containerName)
                        .withExposedPorts(exposedPort)
                        .withHostConfig(HostConfig.newHostConfig()
                                .withPortBindings(portBindings)
                                .withNetworkMode(networkName)
                                .withMemory(512 * 1024 * 1024L)
                                .withMemorySwap(1L * 1024 * 1024 * 1024)
                                .withCpuShares(256)
                                .withCpusetCpus("0")
                        )
                        .withEnv(List.of(
                                "MYSQL_HOST=" + MYSQL_HOST,
                                "MYSQL_USER=" + MYSQL_USER,
                                "MYSQL_PASSWORD=" + MYSQL_PASSWORD,
                                "MYSQL_DB=" + dbName
                        ))
                        .exec();

                dockerClient.startContainerCmd(container.getId()).exec();
                containerId = container.getId();

                Container createdContainer = dockerClient.listContainersCmd()
                        .withIdFilter(List.of(containerId))
                        .withShowAll(true)
                        .exec()
                        .get(0);

                // 如果端口动态分配，更新 hostPort
                if (!imageName.toLowerCase().contains("sql")) {
                    for (ContainerPort port : createdContainer.getPorts()) {
                        if (port.getPrivatePort() == 3000 && port.getPublicPort() != null) {
                            hostPort = port.getPublicPort();
                            break;
                        }
                    }
                }
            }

            // 保存实例
            ContainerInstance instance = new ContainerInstance();
            instance.setUserId(userId);
            instance.setLabName("共享环境-" + vulnType);
            instance.setVulnType(vulnType);
            instance.setRestartCount(0);
            instance.setLastStatusChangeReason("新建共享环境");
            instance.setImageName(imageName);
            instance.setContainerId(containerId);
            instance.setNetworkId(networkName);
            instance.setCreateTime(Instant.now());
            instance.setExpireTime(Instant.now().plus(durationMinutes, ChronoUnit.MINUTES));
            instance.setStatus("RUNNING");
            instance.setPortMapping(Map.of(3000, hostPort));
            instance.setAccessUrl("https://www.wacyg.fun/lab-" + hostPort);
            return mongoTemplate.save(instance);
        } catch (Exception e) {
            throw new RuntimeException("创建共享环境失败: " + e.getMessage(), e);
        }
    }

}
