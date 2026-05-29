package com.bmgf.service.impl;

import com.bmgf.dao.NewsRepository;
import com.bmgf.po.News;
import com.bmgf.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Override
    public List<News> findAll() {
        return newsRepository.findAll();
    }

    @Override
    public Optional<News> findById(String id) {
        return newsRepository.findById(id);
    }

    @Override
    public News save(News news) {
        if (news.getId() == null) {
            news.setCreateTime(LocalDateTime.now());
        }
        news.setUpdateTime(LocalDateTime.now());
        return newsRepository.save(news);
    }

    @Override
    public void deleteById(String id) {
        newsRepository.deleteById(id);
    }
}
