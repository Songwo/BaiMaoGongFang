package com.bmgf.DTO;


import com.bmgf.po.PortScanResult;
import lombok.Data;
import java.util.List;

@Data
public class PortScanResponse {
    private String target;          // 扫描目标
    private int totalPorts;        // 总扫描端口数
    private int openPorts;         // 开放端口数
    private long scanTime;         // 扫描耗时(ms)
    private int avgResponseTime;   // 平均响应时间(ms)
    private List<PortScanResult> results;  // 扫描结果列表
}