package com.bmgf.dao;

import com.bmgf.po.UserLearningBehavior;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserLearningBehaviorRepository extends MongoRepository<UserLearningBehavior, String> {
    List<UserLearningBehavior> findByUserId(String userId);
    List<UserLearningBehavior> findByUserIdOrderByInteractionTimeDesc(String userId);
    List<UserLearningBehavior> findByUserIdAndContentType(String userId, String contentType);

    // 添加新的查询方法
    List<UserLearningBehavior> findByContentIdIn(List<String> contentIds);
    List<UserLearningBehavior> findByUserIdIn(List<String> userIds);
}
