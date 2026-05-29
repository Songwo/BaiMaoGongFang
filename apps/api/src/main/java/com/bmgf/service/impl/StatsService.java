package com.bmgf.service.impl;

import com.bmgf.dao.StatsRepository;
import com.bmgf.po.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.bmgf.dao.PostRepository;
import com.bmgf.dao.CommentRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
    private MongoTemplate mongoTemplate;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    // 获取今日日期字符串，格式：yyyy-MM-dd
    private String getTodayDateStr() {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }

    public Map<String, Long> getStats() {
        String todayStr = getTodayDateStr();
        Stats stats = statsRepository.findByDate(todayStr)
                .orElseGet(() -> {
                    Stats newStats = new Stats();
                    newStats.setDate(todayStr);
                    newStats.setDailyVisits(0);
                    newStats.setUserCount(0);
                    return statsRepository.save(newStats);
                });

        Map<String, Long> result = new HashMap<>();
        result.put("dailyVisits", stats.getDailyVisits());
        result.put("userCount", stats.getUserCount());
        return result;
    }

    public void incrementVisits() {
        String todayStr = getTodayDateStr();
        Query query = new Query(Criteria.where("date").is(todayStr));
        Update update = new Update()
                .inc("dailyVisits", 1)
                .setOnInsert("date", todayStr)
                .setOnInsert("userCount", 0);

        mongoTemplate.upsert(query, update, Stats.class);
    }

    public void incrementUserCount() {
        String todayStr = getTodayDateStr();
        Query query = new Query(Criteria.where("date").is(todayStr));
        Update update = new Update()
                .inc("userCount", 1)
                .setOnInsert("date", todayStr)
                .setOnInsert("dailyVisits", 0);

        mongoTemplate.upsert(query, update, Stats.class);
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
        String todayStr = getTodayDateStr();
        Query query = new Query(Criteria.where("userId").is(userId).and("date").is(todayStr));
        Update update = new Update()
                .inc("postCount", 1)
                .setOnInsert("date", todayStr)
                .setOnInsert("userId", userId)
                .setOnInsert("commentCount", 0);

        mongoTemplate.upsert(query, update, Stats.class);
    }

    public void incrementUserCommentCount(String userId) {
        String todayStr = getTodayDateStr();
        Query query = new Query(Criteria.where("userId").is(userId).and("date").is(todayStr));
        Update update = new Update()
                .inc("commentCount", 1)
                .setOnInsert("date", todayStr)
                .setOnInsert("userId", userId)
                .setOnInsert("postCount", 0);

        mongoTemplate.upsert(query, update, Stats.class);
    }
}
