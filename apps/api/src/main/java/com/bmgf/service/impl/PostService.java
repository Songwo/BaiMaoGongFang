package com.bmgf.service.impl;

import com.bmgf.DTO.PostDTO;
import com.bmgf.po.Post;
import com.bmgf.dao.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    // 获取所有帖子（按时间倒序）
    public List<Post> findAllPosts() {
        return postRepository.findAllByOrderByTimestampDesc();
    }

    // 创建帖子
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    // 更新帖子
    public void savePost(Post post) {
        postRepository.save(post);
    }

    // 删除帖子
    public void deletePost(Post post) {
        postRepository.delete(post);
    }

    // 根据ID查找帖子
    public Post findById(String id) {
        return postRepository.findById(id).orElse(null);
    }

    // 按板块查询帖子（时间倒序）
    public List<Post> findBySection(String section) {
        return postRepository.findBySectionOrderByTimestampDesc(section);
    }

    // 搜索帖子（标题、内容、用户名，忽略大小写）
    public List<Post> searchPosts(String keyword) {
        return postRepository.searchPosts(keyword);
    }

    // 获取热门帖子（按回复数排序）
    public List<Post> findHotPosts(int limit) {
        return postRepository.findAllByOrderByReplyCountDesc(PageRequest.of(0, limit));
    }

    // 统计各板块帖子数量
    public List<PostDTO> countPostBySection() {
        return postRepository.findAll().stream()
                .collect(Collectors.groupingBy(Post::getSection, Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    PostDTO dto = new PostDTO();
                    dto.setSection(e.getKey());
                    dto.setCount(e.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
