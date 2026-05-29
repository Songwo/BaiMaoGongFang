package com.bmgf.dao;

import com.bmgf.po.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    UserProfile findByUserId(String userId);

    @Query("select distinct p from UserProfile p join p.interests i where i in :interests")
    List<UserProfile> findByInterestsIn(@Param("interests") List<String> interests);
}
