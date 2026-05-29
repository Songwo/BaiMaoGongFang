package com.bmgf.dao;

import com.bmgf.po.ExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamRecordRepository extends JpaRepository<ExamRecord, String> {
    List<ExamRecord> findByUserId(String userId);

    List<ExamRecord> findByExamId(String examId);

    List<ExamRecord> findByUserIdOrderBySubmitTimeDesc(String userId);

    List<ExamRecord> findByUserIdAndExamId(String userId, String examId);

    // 用户最近 10 次考试记录
    List<ExamRecord> findTop10ByUserIdOrderBySubmitTimeDesc(String userId);

    // 用户某时间段的考试记录
    List<ExamRecord> findByUserIdAndSubmitTimeBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);
}
