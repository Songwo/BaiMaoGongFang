package com.bmgf.dao;

import com.bmgf.po.LearningContent;

import java.util.List;

public interface LearningContentRepositoryCustom {
    /**
     * 查询难度不超过给定值、且分类与给定分类有交集的学习内容。
     * categories 为 jsonb 数组，JSONB 包含运算符与 JDBC 占位符冲突，故按难度查询后在内存做交集过滤。
     */
    List<LearningContent> findByCategoriesInAndDifficultyLevelLessThanEqual(List<String> categories, Integer difficultyLevel);
}
