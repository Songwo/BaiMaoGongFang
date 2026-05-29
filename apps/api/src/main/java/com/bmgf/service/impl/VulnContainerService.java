package com.bmgf.service.impl;
import com.bmgf.po.ComposeEnvironment;
import com.bmgf.po.User;
import com.bmgf.service.MySQLService;
import com.bmgf.util.JwtUtil;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.bmgf.po.ContainerInstance;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class VulnContainerService {
    private final DockerClient dockerClient;
    private final MongoTemplate mongoTemplate;
    private final MySQLService mySQLService;
    private final JwtUtil jwtUtil;
    private static final int MAX_RESTART_ATTEMPTS = 3;
    private static final int MONITOR_INTERVAL = 5000;
    private static final int MYSQL_WAIT_TIMEOUT = 120;
    private static final int SERVICE_START_DELAY = 12;
    private static final int MAX_BACKEND_WAIT_RETRIES = 5; // 最大等待次数
    private static final int BACKEND_WAIT_DELAY_MS = 2000;   // 每次等待2秒
    private static final Logger log = LoggerFactory.getLogger(VulnContainerService.class);
    private final UserService userService;
    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    private String createNetwork(String networkName) {
        try {
            try {
                dockerClient.inspectNetworkCmd().withNetworkId(networkName).exec();
                return networkName;
            } catch (NotFoundException e) {
                CreateNetworkResponse response = dockerClient.createNetworkCmd()
                        .withName(networkName)
                        .withDriver("bridge")
                        .exec();
                return response.getId();
            }
        } catch (Exception e) {
            throw new RuntimeException("创建网络失败: " + e.getMessage(), e);
        }
    }
    @Async
    public CompletableFuture<ComposeEnvironment> createComposeEnvironmentAsync(
            User user, List<ServiceSpec> services, int durationMinutes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String envId = UUID.randomUUID().toString();
                Map<String, String> backendEnv = services.stream()
                        .filter(s -> s.getServiceName().toLowerCase().contains("backend"))
                        .map(ServiceSpec::getEnv)
                        .findFirst()
                        .orElse(new HashMap<>());
                configureServiceEnvironments(services, backendEnv);
                String networkName = String.format("user-%s-env-%s", user.getId(), envId);
                String networkId = createNetwork(networkName);
                Map<String, String> containerIds = new LinkedHashMap<>();
                createAllContainers(services, networkId, containerIds);
                startServicesWithDependencies(services, containerIds);
                ComposeEnvironment env = new ComposeEnvironment();
                env.setUserId(user.getId());
                env.setNetworkId(networkId);
                env.setServices(containerIds);
                env.setCreateTime(Instant.now());
                env.setExpireTime(Instant.now().plus(durationMinutes, ChronoUnit.MINUTES));
                env.setStatus("RUNNING");
                env.setImageNames(services.stream()
                        .map(ServiceSpec::getImage)
                        .collect(Collectors.toSet()));
                ComposeEnvironment saved = mongoTemplate.save(env);
                return saved;
            } catch (Exception e) {
                log.error("环境创建异常: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, taskExecutor);
    }


    private void cleanupResources(Collection<String> containerIds, String networkId) {
        containerIds.forEach(id -> {
            try { dockerClient.removeContainerCmd(id).withForce(true).exec(); }
            catch (Exception ex) { log.error("清理容器 {} 失败", id, ex); }
        });
        try { dockerClient.removeNetworkCmd(networkId).exec(); }
        catch (Exception ex) { log.error("清理网络 {} 失败", networkId, ex); }
    }

    private void createAllContainers(List<ServiceSpec> services,
                                     String networkId,
                                     Map<String, String> containerIds) throws InterruptedException {
        for (ServiceSpec spec : services) {
            try {
                log.info("正在创建容器: {}", spec.getServiceName());
                CreateContainerResponse response = createServiceContainer(spec, networkId, networkId);
                containerIds.put(spec.getServiceName(), response.getId());
                log.info("容器 {} 创建成功, ID: {}", spec.getServiceName(), response.getId());
            } catch (Exception e) {
                log.error("创建容器 {} 失败", spec.getServiceName(), e);
                throw e;
            }
        }
    }


    private void startServicesWithDependencies(List<ServiceSpec> services,
                                               Map<String, String> containerIds) throws InterruptedException {

        // 排序: 数据库 -> 后端 -> 前端
        List<ServiceSpec> orderedServices = services.stream()
                .sorted(Comparator.comparingInt(s ->
                        s.getImage().contains("mysql") ? 0 :
                                s.getImage().contains("backend") ? 1 : 2))
                .collect(Collectors.toList());

        for (ServiceSpec spec : orderedServices) {
            String containerId = containerIds.get(spec.getServiceName());
            log.info("正在启动服务: {}", spec.getServiceName());

            try {
                // 如果是前端，需要先检测 backend 是否就绪
                if (spec.getImage().contains("frontend")) {
                    String backendId = containerIds.get("backend");
                    boolean backendReady = false;
                    int retry = 0;
                    String backendName = services.stream()
                            .filter(s -> s.getServiceName().toLowerCase().contains("backend"))
                            .map(ServiceSpec::getServiceName)
                            .findFirst().orElse("backend");
                    while (retry < MAX_BACKEND_WAIT_RETRIES) {
                        if (isPortMapped(backendName, 3000)) {
                            backendReady = true;
                            break;
                        }
                        log.info("检测到 "+backendName+" 尚未就绪，等待 {} 毫秒...", BACKEND_WAIT_DELAY_MS);
                        Thread.sleep(BACKEND_WAIT_DELAY_MS);
                        retry++;
                    }
                    if (!backendReady) {
                        throw new RuntimeException("Backend not ready after waiting, frontend will crash");
                    }
                }

                // 启动容器（对 NotModifiedException 容错处理）
                try {
                    dockerClient.startContainerCmd(containerId).exec();
                    log.info("容器 {} 启动命令已发送", spec.getServiceName());
                } catch (NotModifiedException nme) {
                    log.warn("容器 {} 已经处于预期状态，忽略启动异常: {}", spec.getServiceName(), nme.getMessage());
                }

                // 对MySQL进行特殊处理 - 等待完全就绪
                if (spec.getImage().contains("mysql")) {
                    waitForMySQLReady(containerId);
                } else {
                    log.info("等待 {} 秒确保服务稳定...", SERVICE_START_DELAY);
                    Thread.sleep(SERVICE_START_DELAY * 1000);
                }

                // 检查容器状态
                Thread.sleep(2000); // 稍微等一下
                InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                if (!inspect.getState().getRunning()) {
                    log.error("容器 {} 启动失败，当前状态: {}", containerId, inspect.getState().getStatus());
                    debugContainerLogs(containerId); // 打印关键日志
                    throw new RuntimeException("容器启动失败: " + inspect.getState().getStatus());
                }

                log.info("服务 {} 启动成功", spec.getServiceName());
            } catch (Exception e) {
                log.error("启动服务 {} 失败", spec.getServiceName(), e);
                debugContainerLogs(containerId);
                throw e;
            }
        }
    }


    /**
     * 等待MySQL容器完全就绪
     * @param containerId MySQL容器ID
     */
    private void waitForMySQLReady(String containerId) {
        log.info("Checking if MySQL container {} is running...", containerId);
        waitForContainerState(containerId, "running", 30);
        log.info("MySQL container {} is running. Waiting for it to be ready...", containerId);
        Instant start = Instant.now();
        Instant end = start.plusSeconds(MYSQL_WAIT_TIMEOUT);
        String password = getMySQLPassword(containerId);
        log.debug("MySQL root password for container {}: {}", containerId, password);

        while (Instant.now().isBefore(end)) {
            try {
                // 使用 bash 内执行 MySQL 测试连接
                String[] testCmd = {
                        "bash", "-c",
                        String.format("mysql -h 127.0.0.1 -uroot -p%s -e 'SELECT 1'", password)
                };
                String execId = dockerClient.execCreateCmd(containerId)
                        .withCmd(testCmd)
                        .withAttachStderr(true)
                        .withAttachStdout(true)
                        .exec().getId();
                LogCollectorCallback callback = new LogCollectorCallback();
                dockerClient.execStartCmd(execId)
                        .exec(callback)
                        .awaitCompletion(10, TimeUnit.SECONDS);
                int exitCode = dockerClient.inspectExecCmd(execId).exec().getExitCode();
                String logs = callback.getLogs();

                if (exitCode == 0) {
                    log.info("MySQL connection test successful (took {} seconds)",
                            ChronoUnit.SECONDS.between(start, Instant.now()));
                    return;
                }

                // 检查 mysqld 进程是否启动
                if (isMySQLProcessRunning(containerId)) {
                    log.info("MySQL process started (took {} seconds)",
                            ChronoUnit.SECONDS.between(start, Instant.now()));
                    return;
                }

                Thread.sleep(2000);
            } catch (Exception e) {
                log.debug("Health check exception: {}", e.getMessage());
            }
        }

        debugContainerLogs(containerId);
        throw new RuntimeException("MySQL startup timed out");
    }

    private void waitForContainerState(String containerId, String targetState, int timeoutSeconds) {
        Instant end = Instant.now().plusSeconds(timeoutSeconds);
        while (Instant.now().isBefore(end)) {
            InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
            if (targetState.equals(inspect.getState().getStatus())) {
                return;
            }
        }
        throw new RuntimeException("等待容器状态超时");
    }

    private boolean isMySQLProcessRunning(String containerId) {
        try {
            String execId = dockerClient.execCreateCmd(containerId)
                    .withCmd("pgrep", "mysqld")
                    .exec().getId();

            LogCollectorCallback callback = new LogCollectorCallback();
            dockerClient.execStartCmd(execId).exec(callback).awaitCompletion(2, TimeUnit.SECONDS);

            return dockerClient.inspectExecCmd(execId).exec().getExitCode() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // 修改后的 isPortListening 方法
    private boolean isPortMapped(String containerId, int port) {
        try {
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
            Map<ExposedPort, Ports.Binding[]> portBindings = containerInfo.getNetworkSettings().getPorts().getBindings();

            if (portBindings != null) {
                for (Map.Entry<ExposedPort, Ports.Binding[]> entry : portBindings.entrySet()) {
                    ExposedPort exposedPort = entry.getKey();
                    Ports.Binding[] bindings = entry.getValue();

                    // 检查容器是否映射了指定端口，且有绑定主机端口
                    if (exposedPort.getPort() == port && bindings != null && bindings.length > 0) {
                        for (Ports.Binding binding : bindings) {
                            if (binding.getHostPortSpec() != null && !binding.getHostPortSpec().isEmpty()) {
                                log.debug("容器 {} 端口 {} 已映射到主机端口 {}", containerId, port, binding.getHostPortSpec());
                                return true;
                            }
                        }
                    }
                }
            }

            log.debug("容器 {} 未映射端口 {}", containerId, port);
            return false;
        } catch (Exception e) {
            log.error("检查容器端口映射失败: {}", e.getMessage());
            return false;
        }
    }

    private String getMySQLPassword(String containerId) {
        try {
            InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
            Optional<String> passwordVar = Arrays.stream(inspect.getConfig().getEnv())
                    .filter(env -> env.startsWith("MYSQL_ROOT_PASSWORD="))
                    .findFirst();

            if (passwordVar.isPresent()) {
                return passwordVar.get().split("=", 2)[1];
            } else {
                log.warn("未找到MYSQL_ROOT_PASSWORD环境变量，使用默认密码");
                return "root";
            }
        } catch (Exception e) {
            log.error("获取MySQL密码失败，使用默认密码", e);
            return "root";
        }
    }

    // 日志收集回调类
    private static class LogCollectorCallback extends LogContainerResultCallback {
        private final StringBuilder logs = new StringBuilder();

        @Override
        public void onNext(Frame frame) {
            logs.append(new String(frame.getPayload()));
            super.onNext(frame);
        }

        public String getLogs() {
            return logs.toString();
        }
    }

    /**
     * 创建服务容器
     * @param spec 服务规格
     * @param networkId 网络ID
     * @return 容器创建响应
     */
    private CreateContainerResponse createServiceContainer(ServiceSpec spec,
                                                           String networkId, String networkName) throws InterruptedException {
        // 确保镜像存在
        ensureImageExists(spec.getImage());

        // 端口绑定配置
        Ports portBindings = new Ports();
        spec.getPorts().forEach((hostPort, containerPort) ->
                portBindings.bind(ExposedPort.tcp(containerPort),
                        Ports.Binding.bindPort(hostPort))
        );

        // 环境变量配置
        List<String> envVars = spec.getEnv().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());

        // 创建容器
        return dockerClient.createContainerCmd(spec.getImage())
                .withName(spec.getServiceName())
                .withExposedPorts(spec.getPorts().values().stream()
                        .map(ExposedPort::tcp)
                        .collect(Collectors.toList()))
                .withHostConfig(buildSecureHostConfig(portBindings)
                        .withNetworkMode(networkName))
                .withEnv(envVars)
                .exec();
    }

    /**
     * 配置服务环境变量（使用前端传入 env 数据为主）
     */
    private void configureServiceEnvironments(List<ServiceSpec> services, Map<String, String> frontendEnv) {
        String backendName = services.stream()
                .filter(s -> s.getServiceName().toLowerCase().contains("backend"))
                .map(ServiceSpec::getServiceName)
                .findFirst().orElse("backend");

        services.forEach(spec -> {
            String serviceName = spec.getServiceName().toLowerCase();

            // MySQL 额外初始化参数
            if (serviceName.contains("mysql")) {
                spec.getEnv().putIfAbsent("MYSQL_DEFAULT_AUTHENTICATION_PLUGIN", "mysql_native_password");
                spec.getEnv().putIfAbsent("MYSQL_INITDB_SKIP_TZINFO", "1");
                spec.getEnv().putIfAbsent("MYSQL_DISABLE_FAST_SHUTDOWN", "1");
                spec.getEnv().putIfAbsent("MYSQL_INITDB_WAIT_TIMEOUT", "30");
            }

            // 后端服务配置数据库连接参数（从前端传入的 env 数据中动态获取）
            if (serviceName.contains("backend")) {
                spec.getEnv().put("DB_HOST", frontendEnv.getOrDefault("DB_HOST", "mysql"));
                spec.getEnv().put("DB_PORT", frontendEnv.getOrDefault("DB_PORT", "3306"));
                spec.getEnv().put("DB_NAME", frontendEnv.getOrDefault("DB_NAME", "vulnerable_db"));
                spec.getEnv().put("DB_USER", frontendEnv.getOrDefault("DB_USER", "root"));
                spec.getEnv().put("DB_PASSWORD", frontendEnv.getOrDefault("DB_PASSWORD", "123456"));
                spec.getEnv().put("JDBC_PARAMS", frontendEnv.getOrDefault("JDBC_PARAMS", "useSSL=false&allowPublicKeyRetrieval=true"));
            }

            // 前端服务配置后端地址（确保能访问后端容器）
            if (serviceName.contains("frontend")) {
                String backendUrl = "http://" + backendName + ":3000";
                spec.getEnv().put("VITE_BACKEND_NAME", backendName);
                spec.getEnv().put("NGINX_BACKEND_URL", backendUrl);
                spec.getEnv().put("NGINX_UPSTREAM", backendName);
            }
        });
    }
    private HostConfig buildSecureHostConfig(Ports portBindings) {
        return HostConfig.newHostConfig()
                .withPortBindings(portBindings)
                .withAutoRemove(true)  // 容器停止后自动删除
                .withRestartPolicy(RestartPolicy.noRestart());  // 不自动重启
    }

    private String getVolumeNameForContainer(String containerId) {
        try {
            // 获取容器的详细信息
            InspectContainerResponse containerInspect = dockerClient.inspectContainerCmd(containerId).exec();

            // 获取容器挂载点的卷信息
            List<InspectContainerResponse.Mount> mounts = containerInspect.getMounts();

            // 检查是否有挂载的卷
            if (mounts != null && !mounts.isEmpty()) {
                // 遍历挂载点，获取每个卷的名称
                for (InspectContainerResponse.Mount mount : mounts) {
                    // 如果是Docker命名卷
                    if (mount.getName() != null) {
                        return mount.getName();
                    }
                    // 如果是宿主机路径挂载，可以根据需要处理
                    else {
                        log.warn("容器 {} 使用的是宿主机路径挂载，无法获取卷名", containerId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取容器 {} 的卷信息失败", containerId, e);
        }

        // 如果没有找到卷，返回 null
        return null;
    }


    @Scheduled(fixedRateString = "${monitor.interval:600000}")
    public void monitorContainers() {
        log.info("开始容器健康检查...");
        CompletableFuture<Void> t1 = CompletableFuture.runAsync(
                this::destroyExpiredContainers, taskExecutor);
        CompletableFuture<Void> t2 = CompletableFuture.runAsync(
                this::monitorSingleContainers,  taskExecutor);
        CompletableFuture<Void> t3 = CompletableFuture.runAsync(
                this::monitorComposeEnvironments, taskExecutor);
        log.info("容器健康检查完成");
        CompletableFuture.allOf(t1, t2, t3)
                .whenComplete((r, ex) -> {
                    if (ex != null) {
                        log.error("容器健康检查部分子任务失败", ex);
                    } else {
                        log.info("容器健康检查完成");
                    }
                });
    }
    // 监控独立容器状态
    private void monitorSingleContainers() {
        mongoTemplate.find(
                        Query.query(Criteria.where("status").is("RUNNING")),
                        ContainerInstance.class)
                .forEach(instance -> {
                    String containerId = instance.getContainerId();
                    try {
                        // 检查容器是否存在
                        if (!isContainerExists(containerId)) {
                            log.warn("检测到幽灵容器记录: {}", containerId);
                            forceDestroyContainer(instance);  // 强制销毁容器
                            return;
                        }

                        // 获取容器状态
                        InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                        if (inspect != null && inspect.getState() != null && !inspect.getState().getRunning()) {
                            log.warn("容器 {} 异常停止，状态: {}", containerId, inspect.getState().getStatus());
                            debugContainerLogs(containerId);  // 收集容器日志
                            handleStoppedContainer(instance, inspect.getState().getStatus());  // 处理停止容器
                        }
                    } catch (NotFoundException e) {
                        log.error("容器 {} 未找到: {}", containerId, e.getMessage());  // 处理容器不存在的情况
                    } catch (DockerException e) {
                        log.error("Docker 相关错误处理容器 {}: {}", containerId, e.getMessage());  // 处理 Docker 错误
                    } catch (Exception e) {
                        log.error("监控容器 {} 异常: {}", containerId, e.getMessage(), e);  // 捕获其他异常
                    }
                });
    }
    public void destroyExpiredContainers() {
        Instant now = Instant.now();

        // 1. 查询所有过期且状态为 "RUNNING" 的组合环境
        List<ComposeEnvironment> expiredEnvs = mongoTemplate.find(
                new Query(Criteria.where("expireTime").lt(now)
                        .and("status").is("RUNNING")),
                ComposeEnvironment.class
        );

        for (ComposeEnvironment env : expiredEnvs) {
            String envId     = env.getId();
            String networkId = env.getNetworkId();
            Map<String, String> services = env.getServices();

            // 阶段一：遍历所有服务容器，先获取卷信息，停止 & 删除容器
            List<String> volumesToRemove = new ArrayList<>();
            for (String containerId : services.values()) {
                if (!isContainerExists(containerId)) {
                    log.warn("容器 {} 已不存在，跳过销毁", containerId);
                    continue;
                }

                log.info("容器 {} 已过期，准备销毁", containerId);

                // —— 先获取卷信息
                try {
                    String volumeName = getVolumeNameForContainer(containerId);
                    if (volumeName != null && !volumeName.isEmpty()) {
                        volumesToRemove.add(volumeName);
                    }
                } catch (Exception e) {
                    log.warn("获取容器 {} 挂载卷信息失败: {}", containerId, e.getMessage());
                }

                // —— 停止容器
                try {
                    dockerClient.stopContainerCmd(containerId).exec();
                    log.info("容器 {} 停止成功", containerId);
                } catch (Exception e) {
                    log.error("容器 {} 停止失败: {}", containerId, e.getMessage());
                }

                // —— 删除容器
                try {
                    dockerClient.removeContainerCmd(containerId)
                            .withForce(true)
                            .exec();
                    log.info("容器 {} 删除成功", containerId);
                } catch (Exception e) {
                    log.error("容器 {} 删除失败: {}", containerId, e.getMessage());
                }
            }

            // 阶段二：统一删除所有收集到的卷
            for (String volumeName : volumesToRemove) {
                try {
                    dockerClient.removeVolumeCmd(volumeName).exec();
                    log.info("卷 {} 删除成功", volumeName);
                } catch (Exception e) {
                    log.warn("卷 {} 删除失败: {}", volumeName, e.getMessage());
                }
            }

            // 删除网络（如有）
            if (networkId != null && !networkId.isEmpty()) {
                try {
                    dockerClient.removeNetworkCmd(networkId).exec();
                    log.info("网络 {} 删除成功", networkId);
                } catch (Exception e) {
                    log.error("删除网络 {} 失败: {}", networkId, e.getMessage());
                }
            }

            // 阶段三：仅更新 status 字段为 EXPIRED，保留其余字段
            try {
                Query query = Query.query(Criteria.where("_id").is(envId));
                Update update = new Update().set("status", "EXPIRED");
                mongoTemplate.updateFirst(query, update, ComposeEnvironment.class);
                log.info("组合环境 {} 状态更新为 EXPIRED", envId);
            } catch (Exception e) {
                log.error("更新组合环境 {} 状态失败: {}", envId, e.getMessage(), e);
            }
        }
    }

    // 监控组合环境状态
    private void monitorComposeEnvironments() {
        mongoTemplate.find(
                        Query.query(Criteria.where("status").is("RUNNING")),
                        ComposeEnvironment.class)
                .forEach(env -> {
                    int before = env.getServices().size();
                    env.getServices().entrySet().removeIf(entry -> !isContainerExists(entry.getValue()));  // 移除不存在的服务容器

                    if (env.getServices().size() != before) {
                        mongoTemplate.save(env);
                        log.info("更新组合环境 {} 容器列表", env.getId());
                    }
                });
    }

    // 处理停止的容器
    private void handleStoppedContainer(ContainerInstance instance, String reason) {
        String containerId = instance.getContainerId();
        log.warn("处理停止容器: {}, 原因: {}", containerId, reason);

        if (!isContainerExists(containerId)) {
            forceDestroyContainer(instance);  // 容器不存在时强制销毁
            return;
        }

        if (instance.getRestartCount() < MAX_RESTART_ATTEMPTS) {
            try {
                try {
                    dockerClient.startContainerCmd(containerId).exec();  // 重启容器
                    log.info("容器 {} 重启命令已发送", containerId);
                } catch (NotModifiedException nme) {
                    log.warn("容器 {} 已经在预期状态，忽略启动异常: {}", containerId, nme.getMessage());
                }
                instance.setRestartCount(instance.getRestartCount() + 1);  // 更新重启次数
                mongoTemplate.save(instance);
                log.info("容器 {} 重启成功 (第 {} 次)", containerId, instance.getRestartCount());
            } catch (Exception e) {
                log.error("重启容器 {} 失败", containerId, e);
                forceDestroyContainer(instance);  // 如果重启失败，强制销毁容器
            }
        } else {
            log.warn("容器 {} 达到最大重启次数，强制销毁", containerId);
            forceDestroyContainer(instance);  // 达到最大重启次数后强制销毁
        }
    }

    // 收集容器日志用于调试
    private void debugContainerLogs(String containerId) {
        if (!isContainerExists(containerId)) {
            log.warn("容器 {} 不存在，无法收集日志", containerId);
            return;
        }

        try {
            StringBuilder logs = new StringBuilder();
            int sinceSeconds = (int) Instant.now().minus(5, ChronoUnit.MINUTES).getEpochSecond();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true).withStdErr(true)
                    .withTail(100).withSince(sinceSeconds)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            logs.append(new String(frame.getPayload())).append("\n");
                        }
                    }).awaitCompletion(10, TimeUnit.SECONDS);

            if (logs.length() > 0) {
                log.info("容器 {} 日志:\n{}", containerId, logs);
            }
        } catch (Exception e) {
            log.error("收集容器 {} 日志失败", containerId, e);  // 错误日志收集
        }
    }

    private void forceDestroyContainer(ContainerInstance instance) {
        if (instance == null || instance.getContainerId() == null) {
            log.error("无效的容器实例");
            return;
        }

        String containerId = instance.getContainerId();
        String userId      = instance.getUserId();  // 从实例中获取 userId

        try {
            // 检查容器是否存在
            if (!isContainerExists(containerId)) {
                log.warn("容器 {} 已不存在，跳过销毁操作", containerId);
                return;
            }

            // 停止容器
            try {
                dockerClient.stopContainerCmd(containerId)
                        .withTimeout(10)
                        .exec();
                log.info("容器 {} 已停止", containerId);
            } catch (NotFoundException e) {
                log.warn("容器 {} 未找到，跳过停止操作", containerId);
            }

            // 强制删除容器
            try {
                dockerClient.removeContainerCmd(containerId)
                        .withForce(true)
                        .exec();
                log.info("容器 {} 销毁完成", containerId);
            } catch (NotFoundException e) {
                log.warn("容器 {} 未找到，跳过销毁操作", containerId);
            }

            // 更新容器实例状态并保存
            instance.setStatus("DESTROYED");
            instance.setExpireTime(Instant.now());
            mongoTemplate.save(instance);
            log.info("容器 {} 状态更新为 DESTROYED", containerId);

            // 更新用户的 activeLabs 列表
            if (userId != null) {
                User user = mongoTemplate.findById(userId, User.class);
                if (user != null && user.getActiveLabs() != null) {
                    boolean removed = user.getActiveLabs()
                            .removeIf(ci -> containerId.equals(ci.getContainerId()));
                    if (removed) {
                        mongoTemplate.save(user);
                        log.info("用户 {} 的活动实验室列表移除容器 {}", user.getUsername(), containerId);
                    } else {
                        log.warn("用户 {} 活动实验室中未找到容器 {}", user.getUsername(), containerId);
                    }
                }
            }

        } catch (DockerException e) {
            log.error("Docker 错误，销毁容器 {} 失败: {}", containerId, e.getMessage());
        } catch (Exception e) {
            log.error("销毁容器 {} 时发生未知错误: {}", containerId, e.getMessage(), e);
        }
    }

    private boolean isContainerExists(String containerId) {
        try {
            dockerClient.inspectContainerCmd(containerId).exec();
            return true;  // 容器存在
        } catch (NotFoundException e) {
            return false;  // 容器不存在
        } catch (DockerException e) {
            log.error("检查容器 {} 是否存在时出现错误: {}", containerId, e.getMessage());
            return false;  // 如果出现其他 Docker 错误，认为容器不存在
        } catch (Exception e) {
            log.error("检查容器 {} 是否存在时发生未知错误: {}", containerId, e.getMessage(), e);
            return false;  // 其他未知错误处理
        }
    }


    // 确保镜像存在，不存在则拉取
    private void ensureImageExists(String imageName) throws InterruptedException, DockerException {
        try {
            dockerClient.inspectImageCmd(imageName).exec();
        } catch (NotFoundException e) {
            dockerClient.pullImageCmd(imageName)
                    .start()
                    .awaitCompletion(5, TimeUnit.MINUTES);  // 拉取镜像
        }
    }

    // 构建安全的主机配置

    /**
     * 服务规格定义类
     */
    @Data
    public static class ServiceSpec {
        private String serviceName;
        private String image;
        private Map<Integer, Integer> ports = new HashMap<>();
        private Map<String, String> env = new HashMap<>();
        private List<String> dependsOn = new ArrayList<>(); // 显式声明依赖
    }
}