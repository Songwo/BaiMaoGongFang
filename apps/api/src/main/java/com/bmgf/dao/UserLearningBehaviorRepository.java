package com.bmgf.dao;

import com.bmgf.po.UserLearningBehavior;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLearningBehaviorRepository extends JpaRepository<UserLearningBehavior, String> {
    List<UserLearningBehavior> findByUserId(String userId);

    List<UserLearningBehavior> findByUserIdOrderByInteractionTimeDesc(String userId);

    List<UserLearningBehavior> findByUserIdAndContentType(String userId, String contentType);

    List<UserLearningBehavior> findByContentIdIn(List<String> contentIds);

    List<UserLearningBehavior> findByUserIdIn(List<String> userIds);
}
