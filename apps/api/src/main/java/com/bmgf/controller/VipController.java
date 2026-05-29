package com.bmgf.controller;
import com.bmgf.po.*;
import com.bmgf.service.impl.*;
import com.bmgf.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/vip")
@RequiredArgsConstructor
public class VipController {
    private final VulnContainerService containerService;
    private final UserService userService;
    @Autowired
    private  JwtUtil jwtUtil;

    @Qualifier("taskExecutor")
    @Autowired
    private Executor taskExecutor;
    @PostMapping("/create-compose")
    public CompletableFuture<ResponseEntity<Result<?>>> createComposeLabAsync(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LabController.ComposeCreateRequest request) {

        // 提取 token
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        if (token == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Result.error("认证失败: Authorization头缺失或格式错误"))
            );
        }
        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Result.error("认证失败: Token无效或已过期"))
            );
        }
        // 获取用户名
        String username;
        try {
            username = containerService.getUsernameFromToken(token);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Result.error("认证失败: Token解析失败"))
            );
        }
        // 查找用户
        User user = userService.findByUsername(username);
        if (user == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Result.error("认证失败: 用户不存在"))
            );
        }
        boolean isVip = user.getRoles() != null &&
                user.getRoles().stream()
                        .anyMatch(role -> role.equalsIgnoreCase("role_vip"));
        if (!isVip) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Result.error("权限不足: 仅限VIP用户访问"))
            );
        }
        // 验证请求
        if (request.getServices() == null || request.getServices().isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest()
                            .body(Result.error("无效请求: 至少需要一个服务"))
            );
        }
        // 构建有序服务规格
        Map<String, LabController.ServiceDefinition> serviceMap = request.getServices().stream()
                .collect(Collectors.toMap(LabController.ServiceDefinition::getServiceName, s -> s));
        List<LabController.ServiceDefinition> orderedDefs = resolveServiceDependencies(serviceMap);

        List<VulnContainerService.ServiceSpec> specs = orderedDefs.stream()
                .map(s -> {
                    VulnContainerService.ServiceSpec spec = new VulnContainerService.ServiceSpec();
                    spec.setServiceName(s.getServiceName());
                    spec.setImage(s.getImage());
                    spec.setPorts(s.getPorts());
                    spec.setEnv(s.getEnv());
                    return spec;
                })
                .collect(Collectors.toList());
        SecurityContext originalContext = SecurityContextHolder.getContext();
        return containerService.createComposeEnvironmentAsync(user, specs, request.getDuration())
                .thenApplyAsync(env -> {
                    SecurityContextHolder.setContext(originalContext);
                    LabController.ComposeEnvironmentDTO dto = convertComposeToDTO(env);
                    return ResponseEntity.<Result<?>>ok(Result.success(dto));
                }, taskExecutor)
                .exceptionallyAsync(ex -> {
                    SecurityContextHolder.setContext(originalContext);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.error("创建失败", cause);
                    return ResponseEntity.<Result<?>>status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Result.error(cause.getMessage()));
                }, taskExecutor);
    }
    private LabController.ComposeEnvironmentDTO convertComposeToDTO(ComposeEnvironment env) {
        LabController.ComposeEnvironmentDTO dto = new LabController.ComposeEnvironmentDTO();
        dto.setEnvironmentId(env.getId());
        dto.setServices(env.getServices().keySet());
        dto.setExpireTime(env.getExpireTime());
        dto.setStatus(env.getStatus());
        return dto;
    }
    private Map<String, Object> errorResponse(String error, String message) {
        return Map.of(
                "error", error,
                "message", message,
                "timestamp", Instant.now()
        );
    }
    private List<LabController.ServiceDefinition> resolveServiceDependencies(Map<String, LabController.ServiceDefinition> serviceMap) {
        List<LabController.ServiceDefinition> ordered = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        for (LabController.ServiceDefinition svc : serviceMap.values()) {
            processServiceDependencies(svc, serviceMap, ordered, processed);
        }
        return ordered;
    }
    private void processServiceDependencies(LabController.ServiceDefinition service,
                                            Map<String, LabController.ServiceDefinition> serviceMap,
                                            List<LabController.ServiceDefinition> ordered,
                                            Set<String> processed) {
        if (!processed.add(service.getServiceName())) return;
        for (String dep : service.getDependsOn()) {
            LabController.ServiceDefinition d = serviceMap.get(dep);
            if (d != null) processServiceDependencies(d, serviceMap, ordered, processed);
        }
        ordered.add(service);
    }
    // 请求/响应 DTO
    @Data
    public static class ComposeCreateRequest {
        private List<LabController.ServiceDefinition> services;
        private int duration = 60;
    }
    @Data
    public static class ServiceDefinition {
        private String serviceName;
        private String image;
        private Map<Integer, Integer> ports;
        private Map<String, String> env = new HashMap<>();
        private List<String> dependsOn = new ArrayList<>();
    }
    @Data
    public static class ComposeEnvironmentDTO {
        private String environmentId;
        private Set<String> services;
        private Instant expireTime;
        private String status;
    }
}
