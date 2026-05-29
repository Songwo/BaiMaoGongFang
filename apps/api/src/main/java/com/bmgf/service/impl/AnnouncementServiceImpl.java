package com.bmgf.service.impl;

import com.bmgf.dao.AnnouncementRepository;
import com.bmgf.po.Announcement;
import com.bmgf.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Override
    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findByIsActiveTrueOrderByCreateTimeDesc();
    }

    @Override
    public Announcement getAnnouncementDetail(String id) {
        return announcementRepository.findByIdAndIsActiveTrue(id);
    }

    @Override
    public Announcement createAnnouncement(Announcement announcement, String creatorId) {
        announcement.setCreateTime(new Date());
        announcement.setCreatorId(creatorId);
        announcement.setIsActive(true);
        return announcementRepository.save(announcement);
    }
}