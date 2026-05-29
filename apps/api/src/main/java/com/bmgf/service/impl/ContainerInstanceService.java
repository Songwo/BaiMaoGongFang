package com.bmgf.service.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ContainerInstanceService {
    @Autowired
    private MongoTemplate mongoTemplate;
    public boolean checkIfUserCreatedContainer(String userId, String imageName) {
        Query query = new Query();

        // 构造 user.$id 的查询条件
        query.addCriteria(Criteria.where("userId").is(userId));

        // 镜像名条件
        query.addCriteria(Criteria.where("imageName").is(imageName));

        // 判断是否存在该记录
        return mongoTemplate.exists(query, "container_instances");
    }

}
