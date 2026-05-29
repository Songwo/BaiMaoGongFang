// ComposeEnvironment.java
package com.bmgf.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.*;

@Data
@Document(collection = "compose_environments")
public class ComposeEnvironment {
    @Id
    private String id;

    private String userId;
    private String networkId;
    private Map<String, String> services = new HashMap<>(); // 服务名 -> 容器ID
    private Instant createTime;
    private Instant expireTime;
    private String status;
    private Set<String> imageNames = new HashSet<>();
    private Map<String, Integer> restartCounts = new HashMap<>(); // 容器ID -> 重启次数

    /**
     * 增加容器重启计数
     */
    public void incrementRestartCount(String containerId) {
        restartCounts.put(containerId,
                restartCounts.getOrDefault(containerId, 0) + 1);
    }

    /**
     * 获取容器重启次数
     */
    public int getRestartCount(String containerId) {
        return restartCounts.getOrDefault(containerId, 0);
    }

    /**
     * 移除服务（通过容器ID）
     */
    public void removeServiceByContainerId(String containerId) {
        services.entrySet().removeIf(entry ->
                entry.getValue().equals(containerId));
        restartCounts.remove(containerId);
    }

    /**
     * 检查是否为空环境
     */
    public boolean isEmpty() {
        return services.isEmpty();
    }
}