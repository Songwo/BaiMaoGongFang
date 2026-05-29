package com.bmgf.controller;

import com.bmgf.po.Changelog;
import com.bmgf.service.ChangelogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/changelog")
public class ChangelogController {

    @Autowired
    private ChangelogService changelogService;

    @GetMapping("/list")
    public ResponseEntity<List<Changelog>> getChangelogs() {
        return ResponseEntity.ok(changelogService.getActiveChangelogs());
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Changelog> getChangelogDetail(@PathVariable String id) {
        return ResponseEntity.ok(changelogService.getChangelogDetail(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Changelog> createChangelog(
            @RequestBody Changelog changelog,
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(changelogService.createChangelog(changelog, userId));
    }
}
