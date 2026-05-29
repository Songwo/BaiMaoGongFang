package com.bmgf.dao;

import com.bmgf.po.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, String> {
    List<Announcement> findByIsActiveTrueOrderByCreateTimeDesc();

    Announcement findByIdAndIsActiveTrue(String id);
}
