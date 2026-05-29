package com.bmgf.dao;

import com.bmgf.po.Hole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HoleRepository extends JpaRepository<Hole, String> {
    List<Hole> findAllBy();
}
