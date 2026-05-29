package com.bmgf.po;
// 文件路径：src/main/java/com/itheima/csstudent/vo/RankVO.java
import lombok.Data;
@Data
public class RankVO {
    private String username;
    private int totalScore;
    // 可选：自定义构造方法
    public RankVO(String username, int totalScore) {
        this.username = username;
        this.totalScore = totalScore;
    }
}
