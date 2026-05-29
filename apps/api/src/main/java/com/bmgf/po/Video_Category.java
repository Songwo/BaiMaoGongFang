package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "video_category")
@Getter
@Setter
public class Video_Category {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "challengel1")
    private String challengel1;
    @Column(name = "challengel2")
    private String challengel2;
    @Column(name = "challengel3")
    private String challengel3;
    @Column(name = "challengel4")
    private String challengel4;
    @Column(name = "challengel5")
    private String challengel5;
    @Column(name = "challengel6")
    private String challengel6;
}
