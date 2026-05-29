package com.bmgf.dao;

import com.bmgf.po.HonoraryTitle;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HonoraryTitleRepository extends MongoRepository<HonoraryTitle, String> {
}
