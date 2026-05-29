package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "stats")
public class Stats {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id")
    private String userId;
    // date 为保留字，列名用 stat_date（属性名仍为 date，派生查询不变）
    @Column(name = "stat_date")
    private String date;

    // 用户个人统计
    @Column(name = "post_count")
    private int postCount;        // 发帖数
    @Column(name = "comment_count")
    private int commentCount;     // 评论
    // 整站统计
    @Column(name = "daily_visits")
    private long dailyVisits;     // 日访问量
    @Column(name = "user_count")
    private long userCount;       // 用户总数
    @Column(name = "total_posts")
    private long totalPosts;      // 总帖子数
    @Column(name = "total_comments")
    private long totalComments;   // 总评论数

    @Column(name = "created_date")
    private LocalDate createdDate;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = LocalDate.now();
        }
    }
}
