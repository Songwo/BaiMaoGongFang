package com.bmgf.service;

import com.bmgf.po.News;

import java.util.List;
import java.util.Optional;

public interface NewsService {
    List<News> findAll();
    Optional<News> findById(String id);
    News save(News news);
    void deleteById(String id);
}
