package com.bmgf.service.impl;

import com.bmgf.dao.CategoryRepository;
import com.bmgf.po.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Category findById(String id) {
        if(categoryRepository.findById(id).isEmpty()){
            return null;
        };
        return categoryRepository.findById(id).get();
    }

    public void save(Category vid){
        categoryRepository.save(vid);
    }
}
