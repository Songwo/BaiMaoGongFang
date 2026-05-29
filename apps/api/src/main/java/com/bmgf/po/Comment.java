package com.bmgf.po;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Column(name = "post_id")
    private String postId; // 关联 Post.id

    @Column(name = "author_id")
    private String authorId;
    @Column(name = "honorary_title")
    private String honoraryTitle;

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,30}$", message = "用户名需为3-30位中文、字母、数字或下划线")
    @Column(name = "username")
    private String username;

    @Column(name = "content", columnDefinition = "text")
    private String content;
    @Column(name = "timestamp")
    private Date timestamp; // 创建时间
    @Column(name = "avatar")
    private String avatar;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = new Date();
        }
    }
}
