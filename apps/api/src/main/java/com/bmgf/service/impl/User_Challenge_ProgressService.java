package com.bmgf.service.impl;

import com.bmgf.dao.ChallengeRepository;
import com.bmgf.dao.UserChallengeProgressRepository;
import com.bmgf.po.Challenge;
import com.bmgf.po.CompletedChallenge;
import com.bmgf.po.UserChallengeProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class User_Challenge_ProgressService {
    @Autowired
    private UserChallengeProgressRepository progressRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    public UserChallengeProgress getOrCreateProgress(String userId) {
        return progressRepository.findById(userId)
                .orElseGet(() -> {
                    UserChallengeProgress p = new UserChallengeProgress();
                    p.setUserId(userId);
                    System.out.println("UserId: " + userId);
                    // 打印所有关卡ID
                    System.out.println("所有关卡ID: " + challengeRepository.findAll().stream().map(Challenge::getId).toList());
                    // 默认解锁
                    p.getUnlockedChallenges().add("low-1");
                    System.out.println("新建进度，解锁: low-1");
                    return progressRepository.save(p);
                });
    }

    public void completeChallenge(String userId, String challengeId, int score) {
        UserChallengeProgress progress = progressRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户进度不存在"));
        // 防止重复
        boolean already = progress.getCompletedChallenges().stream()
                .anyMatch(c -> c.getChallengeId().equals(challengeId));
        if (!already) {
            CompletedChallenge cc = new CompletedChallenge();
            cc.setChallengeId(challengeId);
            cc.setCompletionTime(System.currentTimeMillis());
            cc.setScore(score);
            progress.getCompletedChallenges().add(cc);
            progressRepository.save(progress);
        }
    }


}
