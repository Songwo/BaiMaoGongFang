package com.bmgf.po;

import lombok.Data;

@Data
public class EnvRequest {
    private String userId;
    private String vulnType;
    private int durationMinutes;
}

