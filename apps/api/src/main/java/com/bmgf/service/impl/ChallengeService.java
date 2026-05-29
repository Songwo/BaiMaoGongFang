package com.bmgf.service.impl;

import com.bmgf.DTO.ChallengeDTO;
import com.bmgf.dao.ChallengeRepository;

import com.bmgf.dao.UserChallengeProgressRepository;
import com.bmgf.po.Challenge;
import com.bmgf.po.CompletedChallenge;
import com.bmgf.po.ImageConfig;
import com.bmgf.po.UserChallengeProgress;
import com.bmgf.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private User_Challenge_ProgressService progressService;
    @Autowired
    private JwtUtil jwtUtil;

    public List<ChallengeDTO> getAllChallengesForUser(String token) {
        String userId = getUserIdFromToken(token);
        System.out.println("getUserIdFromToken 返回: " + userId);
        UserChallengeProgress progress = progressService.getOrCreateProgress(userId);

        Set<String> unlockedIds = progress.getUnlockedChallenges();
        List<CompletedChallenge> completedList = progress.getCompletedChallenges();

        List<Challenge> challenges = challengeRepository.findAll();
        List<ChallengeDTO> result = new ArrayList<>();
        for (Challenge ch : challenges) {
            ChallengeDTO dto = new ChallengeDTO();
            dto.setId(ch.getId());
            dto.setTitle(ch.getTitle());
            dto.setDescription(ch.getDescription());
            dto.setDifficulty(ch.getDifficulty());
            dto.setTask(ch.getTask());
            dto.setFlag(ch.getFlag());
            dto.setImages(ch.getImages());
            dto.setDurationMinutes(ch.getDurationMinutes());
            dto.setUnlocked(unlockedIds.contains(ch.getId()));

            // 查找完成信息
            CompletedChallenge cc = completedList.stream()
                    .filter(c -> c.getChallengeId().equals(ch.getId()))
                    .findFirst().orElse(null);
            if (cc != null) {
                dto.setCompleted(true);
                dto.setCompletionTime(cc.getCompletionTime());
                dto.setScore(cc.getScore());
            } else {
                dto.setCompleted(false);
                dto.setCompletionTime(null);
                dto.setScore(0);
            }
            result.add(dto);
        }
        return result;
    }

    @Autowired
    private UserService userService;
    // 你需要根据token解析userId
    private String getUserIdFromToken(String token) {
        // 你的token解析逻辑
        // 例如：return JwtUtil.getUserId(token);
        String username = jwtUtil.getUsernameFromToken(token);

        return userService.findByUsername(username).getId(); // 这里的 getId() 必须和 user_challenge_progress 里的 userId 一致
    }

    public Optional<Challenge> findByImageName(String imageName) {
        List<Challenge> all = challengeRepository.findAll();
        for (Challenge ch : all) {
            for (ImageConfig img : ch.getImages().values()) {
                if (img.getImage().equals(imageName)) {
                    return Optional.of(ch);
                }
            }
        }
        return Optional.empty();
    }

    @Autowired
    private UserChallengeProgressRepository progressRepository;

    public void unlockNextChallenge(String userId, String nextChallengeId) {
        if (nextChallengeId == null) return;
        UserChallengeProgress progress = progressRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户进度不存在"));
        if (!progress.getUnlockedChallenges().contains(nextChallengeId)) {
            progress.getUnlockedChallenges().add(nextChallengeId);
            progressRepository.save(progress);
        }
    }
}