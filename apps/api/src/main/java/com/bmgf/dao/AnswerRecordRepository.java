package com.bmgf.dao;

import com.bmgf.po.AnswerRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRecordRepository extends JpaRepository<AnswerRecord, String> {
    List<AnswerRecord> findByUserId(String userId);
}
