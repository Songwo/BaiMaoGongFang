package com.bmgf.service.impl;

import com.bmgf.po.VideoItem;
import com.bmgf.DTO.CategoryVideoDTO;
import com.bmgf.dao.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoService {
    @Autowired
    private VideoRepository videoRepository;

    public Page<VideoItem> getVideos(Pageable pageable, String keyword) {
        return videoRepository.findAllByOrderByCreateTimeDesc(pageable, keyword);
    }

    public Page<VideoItem> getVideo(Pageable pageable) {
        return videoRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    public List<CategoryVideoDTO> countVideoByCategory() {
        return videoRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        v -> v.getCategories() == null ? "" : v.getCategories(),
                        Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    CategoryVideoDTO dto = new CategoryVideoDTO();
                    dto.setCategories(e.getKey());
                    dto.setCount(e.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void deleteVideo(String id) {
        videoRepository.deleteById(id);
    }

    public void saveVideo(VideoItem videoItem) {
        videoRepository.save(videoItem);
    }

    public VideoItem findById(String id) {
        return videoRepository.findById(id).orElse(null);
    }
}
