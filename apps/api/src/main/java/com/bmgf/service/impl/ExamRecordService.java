package com.bmgf.service.impl;

import com.bmgf.dao.ExamRecordRepository;
import com.bmgf.po.ExamRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamRecordService {
    @Autowired
    private ExamRecordRepository examRecordRepository;

    public String totalTime(String userId) {
        List<ExamRecord> examRecords = examRecordRepository.findByUserId(userId);
        int totalMinutes = examRecords.stream()
                .filter(record -> record.getCostTime() != null && record.getCostTime() > 0)
                .mapToInt(ExamRecord::getCostTime)
                .sum();
        return String.valueOf(totalMinutes);
    }
}
