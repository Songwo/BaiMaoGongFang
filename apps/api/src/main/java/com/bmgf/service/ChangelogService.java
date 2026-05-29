package com.bmgf.service;

import com.bmgf.po.Changelog;

import java.util.List;

public interface ChangelogService {
    List<Changelog> getActiveChangelogs();
    Changelog getChangelogDetail(String id);
    Changelog createChangelog(Changelog changelog, String creatorId);
}
