package com.bmgf.dao;

import com.bmgf.po.ComposeEnvironment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComposeEnvRepository extends MongoRepository<ComposeEnvironment, String> {
    ComposeEnvironment findByUserId(String userId);
}

