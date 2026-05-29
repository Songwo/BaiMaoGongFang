package com.bmgf.controller;

import com.bmgf.po.Result;
import com.bmgf.service.impl.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class InviteController {
    @Autowired
    private InviteService inviteService;

    @GetMapping("/generateInviteCode")
    public Result<?> generateInviteCode(@RequestHeader("Authorization") String token) {
        try {
            String username = getUsernameFromToken(token); // 从token中获取用户名
            String code = inviteService.generateInviteCode(username);
            return Result.success(code);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/verifyInviteCode")
    public Result<?> verifyInviteCode(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        try {
            String username = getUsernameFromToken(token);
            String inviteCode = request.get("inviteCode");
            boolean success = inviteService.verifyAndUseInviteCode(inviteCode, username);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private String getUsernameFromToken(String token) {
        // 实现从token中获取用户名的逻辑
        // 这里需要根据你的JWT实现来完成
        return null; // 替换为实际实现
    }
}
