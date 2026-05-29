package com.bmgf.dao;

import com.bmgf.po.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, String> {
    // 原 findByAnyImageName（Mongo images.$*.image 查询）无调用方，迁移时移除；
    // 按镜像名查 Challenge 的逻辑在 ChallengeService.findByImageName 以内存遍历实现。
}
