package com.bmgf.dao;

import com.bmgf.po.VideoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<VideoItem, String> {
    // 按分类分页（创建时间倒序）
    @Query("select v from VideoItem v where v.categories = :categories order by v.createTime desc")
    Page<VideoItem> findAllByOrderByCreateTimeDesc(Pageable pageable, @Param("categories") String categories);

    // 全部分页（创建时间倒序）
    Page<VideoItem> findAllByOrderByCreateTimeDesc(Pageable pageable);
}
