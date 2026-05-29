package com.bmgf.dao;

import com.bmgf.po.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    Page<Feedback> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);
}
