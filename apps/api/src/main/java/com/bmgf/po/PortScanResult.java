package com.bmgf.po;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "port_scan_results")
public class PortScanResult {
    @Id
    private String id;
    private String target;          // 扫描目标
    private int port;              // 端口号
    private String status;         // 端口状态（开放/关闭）
    private String service;        // 服务信息
    private String banner;         // Banner信息
    private Date scanTime;         // 扫描时间
    private String userId;         // 用户ID
    private int responseTime;      // 响应时间(ms)
}

