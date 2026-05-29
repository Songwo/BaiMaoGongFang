package com.bmgf.dao;

import com.bmgf.po.HonoraryTitle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HonoraryTitleRepository extends JpaRepository<HonoraryTitle, String> {
    Optional<HonoraryTitle> findByTitle(String title);
}
