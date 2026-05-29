package com.bmgf.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * 容器实例实体类，映射MongoDB中的container_instances集合
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "container_instances")
public class ContainerInstance {

    @Id
    private String id; // MongoDB主键

    @JsonIgnore
    private String userId; // 关联用户ID，敏感信息不序列化

    private String labName;      // 实验名称
    private String vulnType;     // 漏洞类型
    private int restartCount;    // 重启次数
    private String lastStatusChangeReason; // 状态变更原因

    // 容器配置
    private String imageName;             // 镜像名称
    private Map<Integer, Integer> portMapping;  // 端口映射：宿主机端口 -> 容器端口
    private String containerId;           // Docker 容器ID

    private String networkId;             // Docker 网络ID

    // 状态管理
    private Instant createTime;           // 创建时间
    private Instant expireTime;           // 过期时间，自动销毁时间
    private String status;
    private String AccessUrl;// 状态：RUNNING、STOPPED、EXPIRED等

}
