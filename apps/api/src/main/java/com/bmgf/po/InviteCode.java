package com.bmgf.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@Document(collection = "invite_codes")
public class InviteCode {
    @Id
    private String id;
    private String code;           // 邀请码
    private String inviter;        // 邀请人用户名
    private String invitee;        // 被邀请人用户名
    private LocalDateTime createdAt; // 创建时间
    private boolean used;          // 是否已使用
    private LocalDateTime usedAt;  // 使用时间

    // getters and setters
}
