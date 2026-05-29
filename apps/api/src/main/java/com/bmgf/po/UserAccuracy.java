package com.bmgf.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor // 生成全参构造器（不包含 accuracy）
@NoArgsConstructor  // 生成无参构造器
public class UserAccuracy {
    private String userId; // 用户 ID
    private String username; // 用户名
    private int totalAttempts; // 总答题数
    private int correctCount; // 答对题数
    private double accuracy; // 正确率

    // 自定义全参构造器（不包含 accuracy）
    public UserAccuracy(String userId, String username, int totalAttempts, int correctCount) {
        this.userId = userId;
        this.username = username;
        this.totalAttempts = totalAttempts;
        this.correctCount = correctCount;
        this.accuracy = calculateAccuracy(totalAttempts, correctCount); // 自动计算正确率
    }

    // 计算正确率的方法
    private double calculateAccuracy(int totalAttempts, int correctCount) {
        return totalAttempts == 0 ? 0 : (double) correctCount / totalAttempts;
    }
}