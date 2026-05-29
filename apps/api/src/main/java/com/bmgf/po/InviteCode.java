package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "invite_code")
public class InviteCode {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "code")
    private String code;           // 邀请码
    @Column(name = "inviter")
    private String inviter;        // 邀请人用户名
    @Column(name = "invitee")
    private String invitee;        // 被邀请人用户名
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "used")
    private boolean used;          // 是否已使用
    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
