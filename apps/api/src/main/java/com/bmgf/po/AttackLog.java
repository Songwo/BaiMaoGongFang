package com.bmgf.po;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "attack_logs")
public class AttackLog {
    @Id
    private String id;
    private String containerId;
    private String attackType;
    private String payload;
    private Instant timestamp;
}