package com.bmgf.dao;

import com.bmgf.po.Changelog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChangeLogRepository extends JpaRepository<Changelog, String> {
    List<Changelog> findByIsActiveTrueOrderByCreateTimeDesc();

    Changelog findByIdAndIsActiveTrue(String id);
}
