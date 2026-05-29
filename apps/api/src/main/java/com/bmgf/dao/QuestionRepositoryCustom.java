package com.bmgf.dao;

import com.bmgf.po.Question;

import java.util.List;
import java.util.Set;

public interface QuestionRepositoryCustom {

    /**
     * 随机抽取若干道未答题目（等价于原 MongoDB 的 $match $nin + $sample）。
     */
    List<Question> findRandomQuestionsExcluding(Set<String> excludedIds, int size);
}
