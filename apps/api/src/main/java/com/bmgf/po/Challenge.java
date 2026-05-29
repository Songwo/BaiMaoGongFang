package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "challenges")
public class Challenge {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "title")
    private String title;
    @Column(name = "description", columnDefinition = "text")
    private String description;
    @Column(name = "difficulty")
    private int difficulty;
    @Column(name = "task", columnDefinition = "text")
    private String task;
    @Column(name = "flag")
    private String flag;
    @Column(name = "score")
    private int score;

    // 镜像配置映射，jsonb 存储
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", columnDefinition = "jsonb")
    private Map<String, ImageConfig> images;

    @Column(name = "duration_minutes")
    private int durationMinutes;
}
