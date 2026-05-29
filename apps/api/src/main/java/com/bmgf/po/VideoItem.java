package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "video_item")
public class VideoItem {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "title")
    private String title;
    @Column(name = "url", length = 1024)
    private String url;
    @Column(name = "image_url", length = 1024)
    private String imageUrl;
    @Column(name = "description", columnDefinition = "text")
    private String description;
    @Column(name = "categories")
    private String categories;
    @Column(name = "create_time")
    private Date createTime;
}
