package com.bmgf.dao;

import com.bmgf.po.Stats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatsRepository extends JpaRepository<Stats, String> {
    // 站点级统计（无 userId）
    Optional<Stats> findByUserIdIsNullAndDate(String date);

    // 用户级统计
    Optional<Stats> findByUserIdAndDate(String userId, String date);
}
