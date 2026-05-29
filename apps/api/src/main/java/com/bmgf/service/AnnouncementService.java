package com.bmgf.service;

import com.bmgf.po.Announcement;

import java.util.List;

public interface AnnouncementService {
    List<Announcement> getActiveAnnouncements();
    Announcement getAnnouncementDetail(String id);
    Announcement createAnnouncement(Announcement announcement, String creatorId);
}
