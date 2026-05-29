package com.bmgf.DTO;


import lombok.Data;

@Data
public class PortScanRequest {
    private String target;          // 扫描目标
    private int startPort;         // 起始端口
    private int endPort;           // 结束端口
    private String scanType;       // 扫描类型（quick/full/custom）
    private int timeout;           // 超时时间(ms)
}
