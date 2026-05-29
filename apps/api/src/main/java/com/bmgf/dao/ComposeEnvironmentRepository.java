package com.bmgf.dao;

import com.bmgf.po.ComposeEnvironment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComposeEnvironmentRepository extends MongoRepository<ComposeEnvironment, String> {
}
