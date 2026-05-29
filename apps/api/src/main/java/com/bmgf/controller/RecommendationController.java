package com.bmgf.controller;

import com.bmgf.po.LearningContent;
import com.bmgf.po.UserLearningBehavior;
import com.bmgf.service.impl.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LearningContent>> getRecommendations(@PathVariable String userId) {
        // 获取综合推荐结果
        List<LearningContent> recommendations = new ArrayList<>();
        System.out.println("22222222222222222222222222222222:"+recommendations);

        // 添加协同过滤推荐
        recommendations.addAll(recommendationService.getCollaborativeRecommendations(userId));

        // 添加基于内容的推荐
        recommendations.addAll(recommendationService.getContentBasedRecommendations(userId));

        // 添加学习路径推荐
        recommendations.addAll(recommendationService.getLearningPathRecommendations(userId));

        // 去重和排序
        recommendations = recommendations.stream()
                .distinct()
                .sorted(Comparator.comparing(LearningContent::getCreateTime).reversed())
                .collect(Collectors.toList());

        System.out.println("111111111111111111111111111:"+recommendations);

        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/behavior")
    public ResponseEntity<Void> recordBehavior(@RequestBody UserLearningBehavior behavior) {
        recommendationService.recordUserBehavior(behavior);
        System.out.println("33333333333333333333333333333333"+behavior);
        return ResponseEntity.ok().build();
    }
}