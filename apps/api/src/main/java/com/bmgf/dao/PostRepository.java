package com.bmgf.dao;

import com.bmgf.po.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, String> {

    List<Post> findAllByOrderByTimestampDesc();

    List<Post> findBySectionOrderByTimestampDesc(String section);

    List<Post> findAllByOrderByReplyCountDesc(Pageable pageable);

    long countByUsername(String username);

    @Query("select p from Post p where lower(p.title) like lower(concat('%', :kw, '%')) "
            + "or lower(p.content) like lower(concat('%', :kw, '%')) "
            + "or lower(p.username) like lower(concat('%', :kw, '%')) order by p.timestamp desc")
    List<Post> searchPosts(@Param("kw") String keyword);

    @Modifying
    @Query("update Post p set p.honoraryTitle = :ht where p.username = :un")
    void updateHonoraryTitleByUsername(@Param("ht") String honoraryTitle, @Param("un") String username);
}
