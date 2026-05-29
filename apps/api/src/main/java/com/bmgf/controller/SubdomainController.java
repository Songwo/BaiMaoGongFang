package com.bmgf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.swing.text.Document;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class SubdomainController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/subdomain")
    public ResponseEntity<?> searchSubdomain(@RequestBody Map<String, String> request) {
        String domain = request.get("domain");
        if (domain == null || domain.isEmpty()) {
            return ResponseEntity.badRequest().body("域名不能为空");
        }

        try {
            String url = "https://api.hackertarget.com/hostsearch/?q=" + domain;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();

                if (body.contains("error") || body.contains("No records")) {
                    return ResponseEntity.ok(Collections.emptyList());
                }

                List<Map<String, String>> results = new ArrayList<>();

                String[] lines = body.split("\n");
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        Map<String, String> result = new HashMap<>();
                        result.put("subdomain", parts[0]);
                        result.put("ip", parts[1]);
                        result.put("status", "active");
                        results.add(result);
                    }
                }

                return ResponseEntity.ok(results);
            }

            return ResponseEntity.status(response.getStatusCode()).body("查询失败");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("请求错误：" + e.getMessage());
        }
    }

}

