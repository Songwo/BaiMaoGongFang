package com.bmgf.dao;

import java.util.Collection;

/**
 * UserRepository 自定义片段：承载无法用派生查询表达的逻辑。
 */
public interface UserRepositoryCustom {

    /**
     * 累加用户积分/正确次数，并以"集合"语义（去重）追加已完成题目。
     * 等价于原 MongoDB 的 $inc + $addToSet。
     */
    void updateUserStats(String userId, int scoreDelta, int correctDelta, Collection<String> questionIds);
}
