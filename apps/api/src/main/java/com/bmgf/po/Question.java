package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString(); // 唯一标识

    @Column(name = "title", columnDefinition = "text")
    private String title;
    @Column(name = "category")
    private String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    private List<String> options;

    @Column(name = "answer", columnDefinition = "text")
    private String answer; // 储存答案
    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;
    @Column(name = "created_at")
    private String createdAt;
    @Column(name = "updated_at")
    private String updatedAt;
    @Column(name = "score")
    private int score = 10; // 本题分值
    @Column(name = "difficulty_level")
    private int difficultyLevel;
}
