package com.bmgf.dao;

import com.bmgf.po.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, String>, QuestionRepositoryCustom {

    List<Question> findByCategory(String category);

    @Modifying
    @Query("update Question q set q.difficultyLevel = q.difficultyLevel + 1 where q.id = :id")
    void incrementDifficulty(@Param("id") String questionId);

    @Modifying
    @Query("update Question q set q.difficultyLevel = q.difficultyLevel - 1 where q.id = :id")
    void decrementDifficulty(@Param("id") String questionId);

    // 原 findRandomQuestionsByDifficultyExcluding（Mongo $sample）无调用方，迁移时移除；
    // findRandomQuestionsExcluding 见 QuestionRepositoryCustom。
}
