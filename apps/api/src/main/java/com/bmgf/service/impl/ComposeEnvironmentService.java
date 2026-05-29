package com.bmgf.service.impl;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComposeEnvironmentService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 判断某用户是否创建了包含指定镜像名的 Compose 环境
     */
    public boolean checkIfUserCreatedImage(String userId, String imageName) {
        Query query = new Query();

        // 匹配用户 ID
        query.addCriteria(Criteria.where("userId").is(userId));

        // imageNames 是集合字段，匹配其中一个值
        query.addCriteria(Criteria.where("imageNames").is(imageName));

        return mongoTemplate.exists(query, "compose_environments");
    }
}
