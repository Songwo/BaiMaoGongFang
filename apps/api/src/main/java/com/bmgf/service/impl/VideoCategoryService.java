package com.bmgf.service.impl;

import com.bmgf.dao.video_CategoryRepository;
import com.bmgf.po.Video_Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoCategoryService {

    @Autowired
    private video_CategoryRepository repo;

    public Video_Category findById(String id) {
        if(repo.findById(id).isEmpty()){
            return null;
        };
        return repo.findById(id).get();
    }

    public void save(Video_Category vid){
        repo.save(vid);
    }

}
