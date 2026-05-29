package com.bmgf.service;

import com.bmgf.DTO.PortScanRequest;
import com.bmgf.DTO.PortScanResponse;
import com.bmgf.po.PortScanResult;

import java.util.List;

public interface PortScanService {
    PortScanResponse scanPorts(PortScanRequest request, String userId);
    List<PortScanResult> getScanHistory(String userId);
    void deleteScanHistory(String scanId);
}
