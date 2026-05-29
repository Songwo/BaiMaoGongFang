package com.bmgf.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
// CMS特征枚举（单独定义）
enum CMSFeature {
    WORDPRESS(Set.of("/wp-content/", "/wp-admin/"), "WordPress"),
    DRUPAL(Set.of("/sites/all/", "/core/assets/"), "Drupal"),
    JOOMLA(Set.of("/media/com_", "/components/com_"), "Joomla");

    private final Set<String> identifiers;
    private final String name;

    CMSFeature(Set<String> identifiers, String name) {
        this.identifiers = identifiers;
        this.name = name;
    }
    public Set<String> getIdentifiers() {
        return identifiers;
    }
    public String getName() {
        return name;
    }
}

@RestController
@RequestMapping("/cms")
public class UtilController {
    private final RestTemplate restTemplate = new RestTemplate();  // 初始化RestTemplate
    @GetMapping("/detect")
    public ResponseEntity<?> detectCms(@RequestParam String url) {
        try {
            String content = HttpClient.newHttpClient()
                    .send(
                            HttpRequest.newBuilder(URI.create(url)).build(),
                            HttpResponse.BodyHandlers.ofString()
                    ).body();
            for (CMSFeature cms : CMSFeature.values()) {
                if (cms.getIdentifiers().stream().anyMatch(content::contains)) {
                    return ResponseEntity.ok(Map.of("cms", cms.getName()));
                }
            }
            return ResponseEntity.ok(Map.of("cms", "Unknown"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("检测失败: " + e.getMessage());
        }
    }
    @GetMapping("/lookup")
    public ResponseEntity<?> lookupIp(@RequestParam String ip) {
        if (!isValidIp(ip)) {
            return ResponseEntity.badRequest().body("无效的IP地址");
        }
        String apiUrl = "http://ip-api.com/json/" + ip;
        try {
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            if ("success".equals(response.get("status"))) {  // 修正多余括号
                return ResponseEntity.ok(Map.of(
                        "country", response.get("country"),
                        "city", response.get("city"),
                        "isp", response.get("isp")
                ));
            }
            return ResponseEntity.status(500).body("查询失败");
        } catch (RestClientException e) {
            return ResponseEntity.status(503).body("服务暂时不可用");
        }
    }
    private boolean isValidIp(String ip) {
        return ip.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    }
    @GetMapping("/encode")
    public ResponseEntity<?> encode(@RequestParam String input) {
        try {
            String encoded = Base64.getEncoder().encodeToString(input.getBytes());
            return ResponseEntity.ok(Map.of("result", encoded));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("编码失败");
        }
    }
    @GetMapping("/decode")
    public ResponseEntity<?> decode(@RequestParam String input) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            return ResponseEntity.ok(Map.of("result", new String(decodedBytes)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的Base64输入");
        }
    }
}