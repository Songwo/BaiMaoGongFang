package com.bmgf.po;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "skill_level")
    private Integer skillLevel;

    // 兴趣正规化为关联表，支持按兴趣检索
    @ElementCollection
    @CollectionTable(name = "user_profile_interests", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "interest")
    private List<String> interests;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "learning_preferences", columnDefinition = "jsonb")
    private Map<String, Object> learningPreferences;

    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(Integer skillLevel) {
        this.skillLevel = skillLevel;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public Map<String, Object> getLearningPreferences() {
        return learningPreferences;
    }

    public void setLearningPreferences(Map<String, Object> learningPreferences) {
        this.learningPreferences = learningPreferences;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
