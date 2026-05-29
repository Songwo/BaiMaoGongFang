package com.bmgf.po;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
@Data
@Document(collection = "stats")
public class Stats {
    @Id
    private String id;
    private String userId;
    @Indexed(name = "date")
    private String date;
    // 用户个人统计
    private int postCount;        // 发帖数
    private int commentCount;     // 评论
    // 整站统计
    private long dailyVisits;     // 日访问量
    private long userCount;       // 用户总数
    private long totalPosts;      // 总帖子数
    private long totalComments;   // 总评论数
    @CreatedDate
    private LocalDate createdDate; // 创建时间

}