package com.bmgf.util;
public class LearningAdviceUtil {
    public static String generateAdvice(double correctRate, String totalTimeStr, int checkInDays, int totalPass) {
        // 将 totalTime 从字符串（如 "120分钟"）中提取出数值
        int totalMinutes = 0;
        if (totalTimeStr != null && totalTimeStr.matches("\\d+")) {
            totalMinutes = Integer.parseInt(totalTimeStr);
        } else if (totalTimeStr != null && totalTimeStr.contains("分钟")) {
            totalMinutes = Integer.parseInt(totalTimeStr.replaceAll("[^0-9]", ""));
        }
        StringBuilder advice = new StringBuilder("学习建议：\n");
        // 1. 正确率分析
        if (correctRate >= 0.8) {
            advice.append("✅ 答题正确率优秀，继续保持。\n");
        } else if (correctRate >= 0.5) {
            advice.append("⚠️ 正确率一般，建议多进行错题分析，加强薄弱环节。\n");
        } else {
            advice.append("❌ 正确率偏低，建议从基础知识入手，系统复习后再做题。\n");
        }
        // 2. 学习总时长分析
        if (totalMinutes >= 300) {
            advice.append("✅ 学习时长充足，注意劳逸结合。\n");
        } else if (totalMinutes >= 120) {
            advice.append("📈 学习时间一般，建议每天安排1小时学习时间。\n");
        } else {
            advice.append("❗ 学习时间偏少，建议制定固定学习计划，逐步提升。\n");
        }

        // 3. 连续签到分析
        if (checkInDays >= 7) {
            advice.append("✅ 连续签到积极，学习习惯良好。\n");
        } else if (checkInDays >= 3) {
            advice.append("📌 签到天数尚可，建议坚持每日签到保持节奏。\n");
        } else {
            advice.append("🛎️ 签到频率较低，尝试建立规律性的学习习惯。\n");
        }

        // 4. 靶场通关数分析
        if (totalPass >= 10) {
            advice.append("✅ 靶场实战经验丰富，可尝试挑战进阶题目。\n");
        } else if (totalPass >= 3) {
            advice.append("🔰 靶场通关情况一般，建议多参与靶场实战提升能力。\n");
        } else {
            advice.append("🚧 靶场参与较少，建议从入门题目开始积累实战经验。\n");
        }
        return advice.toString();
    }
}

