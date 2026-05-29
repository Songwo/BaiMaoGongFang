package com.bmgf.dao;

import com.bmgf.po.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RewardRepository extends MongoRepository<Reward, String> {
}
