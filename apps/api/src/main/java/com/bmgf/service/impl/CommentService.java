package com.bmgf.service.impl;

import com.bmgf.po.Comment;
import com.bmgf.dao.CommentRepository;
import com.bmgf.dao.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;

    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findByPostIdOrderByTimestampDesc(postId);
    }

    public Comment addComment(Comment comment) {
        // 保存评论
        Comment savedComment = commentRepository.save(comment);
        // 递增对应帖子的回复数
        postRepository.findById(comment.getPostId()).ifPresent(post -> {
            post.setReplyCount(post.getReplyCount() + 1);
            postRepository.save(post);
        });
        return savedComment;
    }

    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    public Comment findById(String id) {
        return commentRepository.findById(id).orElse(null);
    }

    public void deleteCommentById(String id) {
        commentRepository.deleteById(id);
    }
}
