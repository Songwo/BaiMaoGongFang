package com.bmgf.dao;

import com.bmgf.po.Question;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;
import java.util.Set;

public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<Question> findRandomQuestionsExcluding(Set<String> excludedIds, int size) {
        boolean hasExcluded = excludedIds != null && !excludedIds.isEmpty();
        // 空集合时不能生成 not in ()，需分支处理
        String sql = "select * from questions"
                + (hasExcluded ? " where id not in (:ids)" : "")
                + " order by random() limit :size";
        Query query = em.createNativeQuery(sql, Question.class);
        if (hasExcluded) {
            query.setParameter("ids", excludedIds);
        }
        query.setParameter("size", size);
        return query.getResultList();
    }
}
