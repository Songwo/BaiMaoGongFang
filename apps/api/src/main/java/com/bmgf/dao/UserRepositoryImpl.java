package com.bmgf.dao;

import com.bmgf.po.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * UserRepositoryCustom 的实现（Spring Data 按 "Impl" 后缀自动织入）。
 */
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void updateUserStats(String userId, int scoreDelta, int correctDelta, Collection<String> questionIds) {
        User user = em.find(User.class, userId);
        if (user == null) {
            return;
        }
        user.setTotalScore(user.getTotalScore() + scoreDelta);
        user.setCorrectCount(user.getCorrectCount() + correctDelta);
        if (questionIds != null) {
            // 等价于 Mongo $addToSet：去重追加
            for (String qid : questionIds) {
                if (!user.getCompletedQuestions().contains(qid)) {
                    user.getCompletedQuestions().add(qid);
                }
            }
        }
        em.merge(user);
    }
}
