package com.bmgf.controller;

import com.bmgf.DTO.PortScanRequest;
import com.bmgf.DTO.PortScanResponse;
import com.bmgf.po.PortScanResult;
import com.bmgf.service.PortScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portscan")
public class PortScanController {

    @Autowired
    private PortScanService portScanService;

    @PostMapping("/scan")
    public ResponseEntity<PortScanResponse> scanPorts(
            @RequestBody PortScanRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        PortScanResponse response = portScanService.scanPorts(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PortScanResult>> getScanHistory(Authentication authentication) {
        String userId = authentication.getName();
        List<PortScanResult> history = portScanService.getScanHistory(userId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history/{scanId}")
    public ResponseEntity<Void> deleteScanHistory(
            @PathVariable String scanId,
            Authentication authentication) {
        String userId = authentication.getName();
        portScanService.deleteScanHistory(scanId);
        return ResponseEntity.ok().build();
    }
}
