package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "announcement")
public class Announcement {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "title")
    private String title;
    @Column(name = "content", columnDefinition = "text")
    private String content;
    @Column(name = "type")
    private String type; // 公告类型：warning, info, danger 等
    @Column(name = "tag")
    private String tag; // 标签：重要、普通等
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "creator_id")
    private String creatorId; // 创建者ID
    @Column(name = "is_active")
    private Boolean isActive; // 是否激活
}
