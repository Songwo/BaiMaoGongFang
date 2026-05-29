package com.bmgf.service.impl;

import com.bmgf.DTO.PortScanRequest;
import com.bmgf.DTO.PortScanResponse;
import com.bmgf.po.PortScanResult;
import com.bmgf.service.PortScanService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PortScanServiceImpl implements PortScanService {

    private final MongoTemplate mongoTemplate;

    public PortScanServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public PortScanResponse scanPorts(PortScanRequest request, String userId) {
        long startTime = System.currentTimeMillis();
        List<PortScanResult> results = new ArrayList<>();
        AtomicInteger openPorts = new AtomicInteger();
        AtomicInteger totalResponseTime = new AtomicInteger();

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<PortScanResult>> futures = new ArrayList<>();

        // 提交扫描任务
        for (int port = request.getStartPort(); port <= request.getEndPort(); port++) {
            final int currentPort = port;
            Future<PortScanResult> future = executor.submit(() -> {
                long portStartTime = System.currentTimeMillis();
                PortScanResult result = new PortScanResult();
                result.setTarget(request.getTarget());
                result.setPort(currentPort);
                result.setUserId(userId);
                result.setScanTime(new Date());

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(request.getTarget(), currentPort), request.getTimeout());
                    result.setStatus("开放");
                    result.setService(detectService(currentPort));
                    result.setBanner(detectBanner(socket));
                    openPorts.getAndIncrement();
                } catch (Exception e) {
                    result.setStatus("关闭");
                    result.setService("-");
                    result.setBanner("-");
                }

                result.setResponseTime((int) (System.currentTimeMillis() - portStartTime));
                totalResponseTime.addAndGet(result.getResponseTime());
                return result;
            });
            futures.add(future);
        }

        // 收集结果
        for (Future<PortScanResult> future : futures) {
            try {
                PortScanResult result = future.get();
                results.add(result);
                mongoTemplate.save(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        // 构建响应
        PortScanResponse response = new PortScanResponse();
        response.setTarget(request.getTarget());
        response.setTotalPorts(results.size());
        response.setOpenPorts(openPorts.get());
        response.setScanTime(System.currentTimeMillis() - startTime);
        response.setAvgResponseTime(results.isEmpty() ? 0 : totalResponseTime.get() / results.size());
        response.setResults(results);

        return response;
    }

    @Override
    public List<PortScanResult> getScanHistory(String userId) {
        return mongoTemplate.find(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("userId").is(userId)
                ),
                PortScanResult.class
        );
    }

    @Override
    public void deleteScanHistory(String scanId) {
        mongoTemplate.remove(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("id").is(scanId)
                ),
                PortScanResult.class
        );
    }

    private String detectService(int port) {
        // 常见端口服务识别
        switch (port) {
            case 21: return "FTP";
            case 22: return "SSH";
            case 23: return "Telnet";
            case 25: return "SMTP";
            case 53: return "DNS";
            case 80: return "HTTP";
            case 110: return "POP3";
            case 143: return "IMAP";
            case 443: return "HTTPS";
            case 3306: return "MySQL";
            case 3389: return "RDP";
            case 5432: return "PostgreSQL";
            case 27017: return "MongoDB";
            default: return "Unknown";
        }
    }

    private String detectBanner(Socket socket) {
        try {
            // 尝试读取banner信息
            byte[] buffer = new byte[1024];
            int bytesRead = socket.getInputStream().read(buffer);
            if (bytesRead > 0) {
                return new String(buffer, 0, bytesRead).trim();
            }
        } catch (Exception e) {
            // 忽略读取错误
        }
        return "-";
    }
}
