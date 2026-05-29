package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "type")
    private String type;        // 反馈类型：feature, bug, question, other
    @Column(name = "title")
    private String title;       // 反馈标题
    @Column(name = "content", columnDefinition = "text")
    private String content;     // 详细描述
    @Column(name = "contact")
    private String contact;     // 联系方式

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "jsonb")
    private List<String> imageUrls;  // 图片URL列表

    @Column(name = "status")
    private String status;      // 状态：pending, processing, resolved
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
