package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "changelog")
public class Changelog {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "version")
    private String version; // 版本号
    @Column(name = "type")
    private String type; // 类型：feature, fix, improvement 等
    @Column(name = "content", columnDefinition = "text")
    private String content; // 更新内容
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "creator_id")
    private String creatorId; // 创建者ID
    @Column(name = "is_active")
    private Boolean isActive; // 是否激活
}
