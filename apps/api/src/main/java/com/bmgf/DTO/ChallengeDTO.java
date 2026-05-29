package com.bmgf.DTO;

import com.bmgf.po.ImageConfig;
import lombok.Data;
import java.util.Map;

@Data
public class ChallengeDTO {
    private String id;
    private String title;
    private String description;
    private int difficulty;
    private String task;
    private String flag;
    private int score;
    private Map<String, ImageConfig> images;
    private int durationMinutes;
    private boolean unlocked;
    private boolean isCompleted;
    private Long completionTime; // 用Long，前端好处理

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<String, ImageConfig> getImages() {
        return images;
    }

    public void setImages(Map<String, ImageConfig> images) {
        this.images = images;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Long completionTime) {
        this.completionTime = completionTime;
    }
}