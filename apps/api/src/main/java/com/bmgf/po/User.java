// User.java 用户类（JPA 实体；数据层 P3 迁移自 MongoDB）
package com.bmgf.po;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.*;

// 注：实体用 @Getter/@Setter 而非 @Data，避免 JPA 下集合字段参与 equals/hashCode 的隐患
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements Serializable {
    // 基础信息
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "update_time")
    private Date updateTime;

    // 认证信息
    @NotBlank
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,30}$",
            message = "用户名需为3-30位中文、字母、数字或下划线")
    @Column(name = "username", unique = true, nullable = false, length = 30)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "avatar")
    private String avatar = "avatar/17fd8e0a-2bb9-4f82-934d-3f8820fca5f5_ab.jpg";

    // 联系信息
    @Email(regexp = "^(?=.{1,64}@)[a-zA-Z0-9_+&*-]+(\\.[a-zA-Z0-9_+&*-]+)*@"
            + "[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,})$",
            message = "邮箱格式不合法")
    @Column(name = "email")
    private String email;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    @Column(name = "email_verification_token_expiry")
    private Date emailVerificationTokenExpiry;

    // 安全相关：角色正规化为 user_roles 关联表（鉴权时即时加载）
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>(Arrays.asList("USER"));

    @Column(name = "last_login_time")
    private Date lastLoginTime; // 最后登录时间

    // 积分系统
    @Column(name = "total_score")
    private int totalScore = 0; // 总积分
    @Column(name = "correct_count")
    private int correctCount = 0; // 正确次数
    @Column(name = "total_attempts")
    private int totalAttempts = 0; // 总尝试次数

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "completed_questions", columnDefinition = "jsonb")
    private List<String> completedQuestions = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vuln_scores", columnDefinition = "jsonb")
    private Map<String, Integer> vulnScores = new HashMap<>(); // 各漏洞类型得分

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "honorary_title", columnDefinition = "jsonb")
    private Map<String, Integer> HonoraryTitle = new HashMap<>();

    @Column(name = "activity_points")
    private int ActivityPoints; // 活动积分

    @Column(name = "last_check_in_date")
    private Date lastCheckInDate; // 上次签到日期
    @Column(name = "consecutive_check_in_days")
    private int consecutiveCheckInDays = 0; // 连续签到天数

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "owned_rooms", columnDefinition = "jsonb")
    private Set<String> ownedRooms = new HashSet<>(); // 聊天室

    // 实验管理：原 @DBRef，迁移期以 jsonb 内嵌存储（容器属沙箱域，P5 再定关联）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "active_labs", columnDefinition = "jsonb")
    private List<ContainerInstance> activeLabs = new ArrayList<>(); // 正在进行的实验

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "completed_labs", columnDefinition = "jsonb")
    private List<String> completedLabs = new ArrayList<>(); // 已完成实验ID

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (createTime == null) {
            createTime = now;
        }
        updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = new Date();
    }
}
