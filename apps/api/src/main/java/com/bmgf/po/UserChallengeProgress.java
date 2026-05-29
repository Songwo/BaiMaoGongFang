package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "user_challenge_progress")
public class UserChallengeProgress {

    // 主键即用户 ID
    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unlocked_challenges", columnDefinition = "jsonb")
    private Set<String> unlockedChallenges = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "completed_challenges", columnDefinition = "jsonb")
    private List<CompletedChallenge> completedChallenges = new ArrayList<>();
}
