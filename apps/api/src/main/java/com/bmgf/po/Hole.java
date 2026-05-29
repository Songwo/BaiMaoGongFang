package com.bmgf.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hole")
public class Hole {
    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "cve_id")
    private String cve_Id;
    @Column(name = "title")
    private String title;
    @Column(name = "description", columnDefinition = "text")
    private String description;
    @Column(name = "severity")
    private String severity;
    @Column(name = "cvss_score")
    private String cvss_score;
    @Column(name = "published_date")
    private Date published_date;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "affected_versions", columnDefinition = "jsonb")
    private List<String> affected_versions;

    @Column(name = "solutions", columnDefinition = "text")
    private String solutions;
    @Column(name = "created_at")
    private Date created_at;
    @Column(name = "updated_at")
    private Date updated_at;
    @Column(name = "type")
    private String type;
}
