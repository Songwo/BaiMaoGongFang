package com.bmgf.po;

import com.bmgf.controller.QuestionController;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 考试记录实体
@Getter
@Setter
@Entity
@Table(name = "exam_records")
public class ExamRecord {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id")
    private String userId;
    @Column(name = "exam_id")
    private String examId;
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    // 花费时间（分钟）
    @Column(name = "cost_time")
    private Integer CostTime;
    @Column(name = "total_score")
    private int totalScore;
    @Column(name = "correct_count")
    private int correctCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_results", columnDefinition = "jsonb")
    private List<QuestionController.QuestionResult> questionResults;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
