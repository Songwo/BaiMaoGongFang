package com.bmgf.controller;

import com.bmgf.po.News;
import com.bmgf.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping
    public ResponseEntity<List<News>> getAllNews() {
        return ResponseEntity.ok(newsService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable String id) {
        return newsService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<News> createNews(@RequestBody News news) {
        return ResponseEntity.ok(newsService.save(news));
    }

    @PutMapping("/{id}")
    public ResponseEntity<News> updateNews(@PathVariable String id, @RequestBody News news) {
        return newsService.findById(id)
                .map(existingNews -> {
                    news.setId(id);
                    return ResponseEntity.ok(newsService.save(news));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable String id) {
        return newsService.findById(id)
                .map(news -> {
                    newsService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}