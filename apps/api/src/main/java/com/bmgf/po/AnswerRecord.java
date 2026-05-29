package com.bmgf.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "answer_records")
public class AnswerRecord {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id")
    private String userId;
    @Column(name = "question_id")
    private String questionId;
    @Column(name = "exam_id")
    private String examId;
    @Column(name = "user_answer")
    private String userAnswer;
    @Column(name = "correct")
    private boolean correct;
    @Column(name = "score_delta")
    private int scoreDelta; // 本次得分变化
    @Column(name = "answer_time")
    private LocalDateTime answerTime = LocalDateTime.now();
    @Column(name = "cost_time")
    private LocalDateTime costTime;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
