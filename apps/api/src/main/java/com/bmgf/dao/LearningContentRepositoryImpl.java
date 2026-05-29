package com.bmgf.dao;

import com.bmgf.po.LearningContent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.stream.Collectors;

public class LearningContentRepositoryImpl implements LearningContentRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<LearningContent> findByCategoriesInAndDifficultyLevelLessThanEqual(List<String> categories, Integer difficultyLevel) {
        List<LearningContent> candidates = em.createQuery(
                        "select c from LearningContent c where c.difficultyLevel <= :lvl", LearningContent.class)
                .setParameter("lvl", difficultyLevel)
                .getResultList();
        if (categories == null || categories.isEmpty()) {
            return candidates;
        }
        return candidates.stream()
                .filter(c -> c.getCategories() != null
                        && c.getCategories().stream().anyMatch(categories::contains))
                .collect(Collectors.toList());
    }
}
