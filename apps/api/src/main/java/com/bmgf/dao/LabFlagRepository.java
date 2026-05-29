package com.bmgf.dao;

import com.bmgf.po.LabFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabFlagRepository extends JpaRepository<LabFlag, String> {
    Optional<LabFlag> findByImageName(String imageName);
}
