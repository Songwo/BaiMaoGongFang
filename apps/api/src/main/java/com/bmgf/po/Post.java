package com.bmgf.po;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Column(name = "author_id")
    private String authorId; // 关联 User.id

    @Column(name = "username")
    private String username;
    @Column(name = "honorary_title")
    private String honoraryTitle;

    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,30}$", message = "用户名需为3-30位中文、字母、数字或下划线")
    @Column(name = "title")
    private String title;
    @Column(name = "section")
    private String section;
    @Column(name = "content", columnDefinition = "text")
    private String content;
    @Column(name = "cover_image")
    private String coverImage; // 帖子封面图片
    @Column(name = "reply_count")
    private int replyCount = 0;
    @Column(name = "avatar")
    private String avatar;
    @Column(name = "timestamp")
    private Date timestamp; // 创建时间

    // 评论列表为读取时按需填充（评论独立存储于 comments 表），不持久化于帖子
    @Transient
    private List<Comment> comments;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = new Date();
        }
    }
}
