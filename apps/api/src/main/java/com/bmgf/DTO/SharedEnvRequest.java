package com.bmgf.DTO;

public class SharedEnvRequest {
    private String userId;
    private String vulnType;
    private int durationMinutes;

    // 无参构造
    public SharedEnvRequest() {}
    // 带参构造（可选）
    public SharedEnvRequest(String userId, String vulnType, int durationMinutes) {
        this.userId = userId;
        this.vulnType = vulnType;
        this.durationMinutes = durationMinutes;
    }

    // Getter 和 Setter
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVulnType() {
        return vulnType;
    }
    public void setVulnType(String vulnType) {
        this.vulnType = vulnType;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
