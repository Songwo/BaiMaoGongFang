package com.bmgf.dao;

import com.bmgf.po.LearningContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningContentRepository extends JpaRepository<LearningContent, String>, LearningContentRepositoryCustom {
    List<LearningContent> findByLearningStage(String learningStage);

    List<LearningContent> findByIdIn(List<String> ids);
}
