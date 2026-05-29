package com.bmgf.service.impl;

import com.bmgf.dao.LearningContentRepository;
import com.bmgf.dao.UserLearningBehaviorRepository;
import com.bmgf.dao.UserProfileRepository;
import com.bmgf.po.LearningContent;
import com.bmgf.po.UserLearningBehavior;
import com.bmgf.po.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {
    @Autowired
    private UserLearningBehaviorRepository behaviorRepository;
    @Autowired
    private LearningContentRepository contentRepository;
    @Autowired
    private UserProfileRepository profileRepository;

        // 添加公共方法
        public List<LearningContent> getCollaborativeRecommendations(String userId) {
            // 获取用户的学习行为
            List<UserLearningBehavior> userBehaviors = behaviorRepository.findByUserId(userId);

            // 找到相似用户
            List<String> similarUserIds = findSimilarUsers(userId, userBehaviors);

            // 获取相似用户喜欢的内容
            List<String> recommendedContentIds = getSimilarUsersPreferredContent(similarUserIds);

            // 返回推荐内容
            return contentRepository.findByIdIn(recommendedContentIds);
        }

        public List<LearningContent> getContentBasedRecommendations(String userId) {
            // 获取用户画像
            UserProfile userProfile = profileRepository.findByUserId(userId);
            if (userProfile == null) {
                return new ArrayList<>();
            }

            // 根据用户兴趣和技能水平推荐内容
            return contentRepository.findByCategoriesInAndDifficultyLevelLessThanEqual(
                    userProfile.getInterests(),
                    userProfile.getSkillLevel()
            );
        }

        public List<LearningContent> getLearningPathRecommendations(String userId) {
            // 获取用户最近的学习行为
            List<UserLearningBehavior> recentBehaviors = behaviorRepository.findByUserIdOrderByInteractionTimeDesc(userId);

            // 确定用户当前学习阶段
            String currentStage = determineLearningStage(recentBehaviors);

            // 推荐下一阶段的学习内容
            return contentRepository.findByLearningStage(currentStage);
        }


    public void recordUserBehavior(UserLearningBehavior behavior) {
        behaviorRepository.save(behavior);
        // 移除这行，因为用户资料应该由专门的接口更新
        // updateUserProfile(behavior.getUserId());
    }

    // 更新用户画像 - 只在需要时调用
    private void updateUserProfile(String userId) {
        UserProfile profile = profileRepository.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }

        // 只在没有设置这些值时才更新
        if (profile.getSkillLevel() == null) {
            updateSkillLevel(profile);
        }
        if (profile.getInterests() == null || profile.getInterests().isEmpty()) {
            updateInterests(profile);
        }
        if (profile.getLearningPreferences() == null) {
            updateLearningPreferences(profile);
        }

        profileRepository.save(profile);
    }

    // 找到相似用户
    private List<String> findSimilarUsers(String userId, List<UserLearningBehavior> userBehaviors) {
        // 获取用户的学习内容ID列表
        List<String> userContentIds = userBehaviors.stream()
                .map(UserLearningBehavior::getContentId)
                .collect(Collectors.toList());

        // 查找看过相同内容的用户
        List<UserLearningBehavior> similarBehaviors = behaviorRepository.findByContentIdIn(userContentIds);

        // 统计用户相似度
        Map<String, Integer> userSimilarity = new HashMap<>();
        for (UserLearningBehavior behavior : similarBehaviors) {
            if (!behavior.getUserId().equals(userId)) {
                userSimilarity.merge(behavior.getUserId(), 1, Integer::sum);
            }
        }

        // 返回相似度最高的用户ID列表
        return userSimilarity.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 获取相似用户喜欢的内容
    private List<String> getSimilarUsersPreferredContent(List<String> similarUserIds) {
        List<UserLearningBehavior> behaviors = behaviorRepository.findByUserIdIn(similarUserIds);

        // 统计内容受欢迎程度
        Map<String, Integer> contentPopularity = new HashMap<>();
        for (UserLearningBehavior behavior : behaviors) {
            contentPopularity.merge(behavior.getContentId(), 1, Integer::sum);
        }

        // 返回最受欢迎的内容ID列表
        return contentPopularity.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 确定用户当前学习阶段
    private String determineLearningStage(List<UserLearningBehavior> recentBehaviors) {
        if (recentBehaviors.isEmpty()) {
            return "BEGINNER";
        }

        // 分析用户最近的学习行为
        Map<String, Integer> contentTypeCount = new HashMap<>();
        for (UserLearningBehavior behavior : recentBehaviors) {
            contentTypeCount.merge(behavior.getContentType(), 1, Integer::sum);
        }

        // 根据学习内容类型判断学习阶段
        if (contentTypeCount.getOrDefault("QUIZ", 0) > contentTypeCount.getOrDefault("VIDEO", 0)) {
            return "INTERMEDIATE";
        } else if (contentTypeCount.getOrDefault("VULNERABILITY", 0) > 0) {
            return "ADVANCED";
        } else {
            return "BEGINNER";
        }
    }

    // 更新用户技能水平
    private void updateSkillLevel(UserProfile profile) {
        List<UserLearningBehavior> behaviors = behaviorRepository.findByUserId(profile.getUserId());

        // 根据用户行为计算技能水平
        int skillLevel = 1; // 默认级别
        if (!behaviors.isEmpty()) {
            // 计算平均得分
            double avgScore = behaviors.stream()
                    .filter(b -> b.getScore() != null)
                    .mapToDouble(UserLearningBehavior::getScore)
                    .average()
                    .orElse(0.0);

            // 根据平均得分调整技能水平
            if (avgScore > 0.8) {
                skillLevel = 3;
            } else if (avgScore > 0.6) {
                skillLevel = 2;
            }
        }

        profile.setSkillLevel(skillLevel);
    }

    // 更新用户兴趣
    private void updateInterests(UserProfile profile) {
        List<UserLearningBehavior> behaviors = behaviorRepository.findByUserId(profile.getUserId());

        // 统计用户最常访问的内容类别
        Map<String, Integer> categoryCount = new HashMap<>();
        for (UserLearningBehavior behavior : behaviors) {
            if (behavior.getMetadata() != null && behavior.getMetadata().containsKey("category")) {
                String category = (String) behavior.getMetadata().get("category");
                categoryCount.merge(category, 1, Integer::sum);
            }
        }

        // 选择最常访问的类别作为用户兴趣
        List<String> interests = categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        profile.setInterests(interests);
    }

    // 更新学习偏好
    private void updateLearningPreferences(UserProfile profile) {
        List<UserLearningBehavior> behaviors = behaviorRepository.findByUserId(profile.getUserId());

        Map<String, Object> preferences = new HashMap<>();

        // 分析用户偏好的内容类型
        Map<String, Integer> contentTypeCount = new HashMap<>();
        for (UserLearningBehavior behavior : behaviors) {
            contentTypeCount.merge(behavior.getContentType(), 1, Integer::sum);
        }

        // 找出最常使用的内容类型
        String preferredContentType = contentTypeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("VIDEO");

        preferences.put("preferredContentType", preferredContentType);
        profile.setLearningPreferences(preferences);
    }
}
