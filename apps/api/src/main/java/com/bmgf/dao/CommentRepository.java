package com.bmgf.dao;

import com.bmgf.po.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {

    List<Comment> findByPostIdOrderByTimestampDesc(String postId);

    Comment findFirstByPostIdOrderByTimestampDesc(String postId);

    long countByUsername(String username);

    @Modifying
    @Query("update Comment c set c.honoraryTitle = :ht where c.username = :un")
    void updateHonoraryTitleByUsername(@Param("ht") String honoraryTitle, @Param("un") String username);
}
