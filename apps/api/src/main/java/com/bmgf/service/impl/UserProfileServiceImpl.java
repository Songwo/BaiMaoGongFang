package com.bmgf.service.impl;

import com.bmgf.dao.UserProfileRepository;
import com.bmgf.po.UserProfile;
import com.bmgf.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public UserProfile saveUserProfile(UserProfile userProfile) {
        // 确保设置更新时间
        userProfile.setLastUpdateTime(LocalDateTime.now());

        // 如果 skillLevel 是字符串，转换为数字
        if (userProfile.getSkillLevel() == null ) {
            String level = String.valueOf(userProfile.getSkillLevel());
            switch (level) {
                case "beginner":
                    userProfile.setSkillLevel(1);
                    break;
                case "intermediate":
                    userProfile.setSkillLevel(2);
                    break;
                case "advanced":
                    userProfile.setSkillLevel(3);
                    break;
                default:
                    userProfile.setSkillLevel(1);
            }
        }

        // 保存用户资料
        return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile getUserProfile(String userId) {
        return userProfileRepository.findByUserId(userId);
    }
}
