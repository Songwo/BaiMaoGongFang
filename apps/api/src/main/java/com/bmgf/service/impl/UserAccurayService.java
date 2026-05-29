package com.bmgf.service.impl;

import com.bmgf.po.User;
import com.bmgf.po.AnswerRecord;
import com.bmgf.po.UserAccuracy;
import com.bmgf.dao.UserRepository;
import com.bmgf.dao.AnswerRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAccurayService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AnswerRecordRepository answerRecordRepository;

    public List<UserAccuracy> calculateAllUserAccuracy() {
        // 1. 查询所有用户（JPA）
        List<User> users = userRepository.findAll();

        // 2. 遍历每个用户，计算答题正确率
        return users.stream()
                .map(user -> {
                    // 查询用户的答题记录（JPA）
                    List<AnswerRecord> records = answerRecordRepository.findByUserId(user.getId());

                    // 统计总答题数和答对题数
                    int totalAttempts = records.size();
                    int correctCount = (int) records.stream().filter(AnswerRecord::isCorrect).count();

                    // 返回用户信息和正确率
                    return new UserAccuracy(
                            user.getId(),
                            user.getUsername(),
                            totalAttempts,
                            correctCount
                    );
                })
                .collect(Collectors.toList());
    }
}
