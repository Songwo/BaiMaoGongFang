package com.bmgf.controller;

import com.bmgf.DTO.ChallengeDTO;
import com.bmgf.service.impl.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    @GetMapping
    public List<ChallengeDTO> getAllChallenges(@RequestHeader("Authorization") String authHeader) {
        // 增强类型安全校验
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        return challengeService.getAllChallengesForUser(token);
    }
}
