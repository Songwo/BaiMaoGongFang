package com.bmgf.po;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
@Data
public class AnswerRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;       // 用户唯一标识
    @NotBlank(message = "题目ID不能为空")
    private String questionId;   // 题目唯一标识
    @NotBlank(message = "答案不能为空")
    @Pattern(regexp = "^[A-D]$", message = "答案必须是 A/B/C/D 中的一个")
    private String userAnswer;   // 用户提交的答案（如 "A"）
    // 可选字段（根据业务需求扩展）
    private Long timestamp;      // 客户端时间戳（防重放攻击）
    private String deviceId;     // 设备标识（多端登录场景）
}
