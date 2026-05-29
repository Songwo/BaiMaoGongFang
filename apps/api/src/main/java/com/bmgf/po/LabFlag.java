package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "lab_flag")
@Getter
@Setter
public class LabFlag {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "image_name")
    private String imageName;
    @Column(name = "flag")
    private String flag;
}
