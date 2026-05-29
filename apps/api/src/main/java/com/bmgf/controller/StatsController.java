package com.bmgf.controller;

import com.bmgf.po.Result;
import com.bmgf.service.impl.StatsService;
import com.bmgf.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }

    @PostMapping("/visits")
    public ResponseEntity<Void> incrementVisits() {
        statsService.incrementVisits();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users")
    public ResponseEntity<Void> incrementUserCount() {
        statsService.incrementUserCount();
        return ResponseEntity.ok().build();
    }

        @Autowired
        private StatsService userStatsService;

    @Autowired
    private JwtUtil jwtUtil;

        @GetMapping("/{userId}")
        public Result getUserStats(@PathVariable String userId,@RequestHeader("Authorization") String authHeader) {
            String token = authHeader.substring(7); // 去掉"Bearer "前缀
            String username = jwtUtil.getUsernameFromToken(token);
            try {
                Map<String, Integer> stats = userStatsService.getUserStats(username);
                return Result.success(stats);
            } catch (Exception e) {
                return Result.error("获取用户统计信息失败");
            }
        }
    }