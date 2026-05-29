package com.bmgf.dao;

import com.bmgf.po.Stats;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StatsRepository extends MongoRepository<Stats, String> {
    Optional<Stats> findByDate(String date);
    Optional<Stats> findByUserIdAndDate(String userId, LocalDate date);
}