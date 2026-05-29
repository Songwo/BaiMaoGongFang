package com.bmgf.service.impl;

import com.bmgf.dao.StatsRepository;
import com.bmgf.dao.PostRepository;
import com.bmgf.dao.CommentRepository;
import com.bmgf.po.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatsService {

    @Autowired
    private StatsRepository statsRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    // 今日日期字符串 yyyy-MM-dd
    private String getTodayDateStr() {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }

    public Map<String, Long> getStats() {
        String today = getTodayDateStr();
        Stats stats = statsRepository.findByUserIdIsNullAndDate(today)
                .orElseGet(() -> {
                    Stats s = new Stats();
                    s.setDate(today);
                    s.setDailyVisits(0);
                    s.setUserCount(0);
                    return statsRepository.save(s);
                });
        Map<String, Long> result = new HashMap<>();
        result.put("dailyVisits", stats.getDailyVisits());
        result.put("userCount", stats.getUserCount());
        return result;
    }

    public void incrementVisits() {
        String today = getTodayDateStr();
        Stats stats = statsRepository.findByUserIdIsNullAndDate(today)
                .orElseGet(() -> {
                    Stats s = new Stats();
                    s.setDate(today);
                    s.setDailyVisits(0);
                    s.setUserCount(0);
                    return s;
                });
        stats.setDailyVisits(stats.getDailyVisits() + 1);
        statsRepository.save(stats);
    }

    public void incrementUserCount() {
        String today = getTodayDateStr();
        Stats stats = statsRepository.findByUserIdIsNullAndDate(today)
                .orElseGet(() -> {
                    Stats s = new Stats();
                    s.setDate(today);
                    s.setDailyVisits(0);
                    s.setUserCount(0);
                    return s;
                });
        stats.setUserCount(stats.getUserCount() + 1);
        statsRepository.save(stats);
    }

    public Map<String, Integer> getUserStats(String username) {
        long postCount = postRepository.countByUsername(username);
        long commentCount = commentRepository.countByUsername(username);

        Map<String, Integer> stats = new HashMap<>();
        stats.put("postCount", (int) postCount);
        stats.put("commentCount", (int) commentCount);
        return stats;
    }

    public void incrementUserPostCount(String userId) {
        String today = getTodayDateStr();
        Stats stats = statsRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> {
                    Stats s = new Stats();
                    s.setUserId(userId);
                    s.setDate(today);
                    s.setPostCount(0);
                    s.setCommentCount(0);
                    return s;
                });
        stats.setPostCount(stats.getPostCount() + 1);
        statsRepository.save(stats);
    }

    public void incrementUserCommentCount(String userId) {
        String today = getTodayDateStr();
        Stats stats = statsRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> {
                    Stats s = new Stats();
                    s.setUserId(userId);
                    s.setDate(today);
                    s.setPostCount(0);
                    s.setCommentCount(0);
                    return s;
                });
        stats.setCommentCount(stats.getCommentCount() + 1);
        statsRepository.save(stats);
    }
}
