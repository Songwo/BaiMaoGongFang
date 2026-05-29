package com.bmgf.service.impl;

import com.bmgf.DTO.CategoryCount;
import com.bmgf.po.Question;
import com.bmgf.dao.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public List<CategoryCount> countQuestionsByCategory() {
        // 按分类统计题目数（JPA）
        return questionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Question::getCategory, Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    CategoryCount c = new CategoryCount();
                    c.setCategory(e.getKey());
                    c.setCount(e.getValue());
                    return c;
                })
                .collect(Collectors.toList());
    }

    public void saveQuestion(Question question) {
        questionRepository.save(question);
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Question findById(String id) {
        return questionRepository.findById(id).orElse(null);
    }

    public void deleteQuestion(String id) {
        questionRepository.deleteById(id);
    }
}
