package com.bmgf.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// DTO类
@Data
@AllArgsConstructor
 public class ExamRecordDTO {
    private String id;                // 记录ID
    private String examId;           // 考试ID
    private LocalDateTime submitTime; // 提交时间
    private int totalScore;          // 总分
    private int correctCount;        // 正确题数
    private int totalQuestions;      // 总题数
    private double accuracy;         // 正确率
    private String examTitle;        // 考试标题
}

