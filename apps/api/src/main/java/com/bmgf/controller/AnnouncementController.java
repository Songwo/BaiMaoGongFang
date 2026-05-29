package com.bmgf.controller;

import com.bmgf.po.Announcement;
import com.bmgf.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/list")
    public ResponseEntity<List<Announcement>> getAnnouncements() {
        return ResponseEntity.ok(announcementService.getActiveAnnouncements());
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Announcement> getAnnouncementDetail(@PathVariable String id) {
        return ResponseEntity.ok(announcementService.getAnnouncementDetail(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Announcement> createAnnouncement(
            @RequestBody Announcement announcement,
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(announcementService.createAnnouncement(announcement, userId));
    }
}
