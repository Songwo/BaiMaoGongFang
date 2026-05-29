package com.bmgf.service.impl;

import com.bmgf.dao.ChangeLogRepository;
import com.bmgf.po.Changelog;
import com.bmgf.service.ChangelogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class ChangelogServiceImpl implements ChangelogService {

    @Autowired
    private ChangeLogRepository changelogRepository;

    @Override
    public List<Changelog> getActiveChangelogs() {
        return changelogRepository.findByIsActiveTrueOrderByCreateTimeDesc();
    }

    @Override
    public Changelog getChangelogDetail(String id) {
        return changelogRepository.findByIdAndIsActiveTrue(id);
    }

    @Override
    public Changelog createChangelog(Changelog changelog, String creatorId) {
        changelog.setCreateTime(new Date());
        changelog.setCreatorId(creatorId);
        changelog.setIsActive(true);
        return changelogRepository.save(changelog);
    }
}
