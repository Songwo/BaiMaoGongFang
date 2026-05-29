package com.bmgf.dao;

import com.bmgf.po.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, UserRepositoryCustom {

    Optional<User> findByUsername(String username);

    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdString(@Param("id") String id);

    Optional<User> findByEmailVerificationToken(String emailVerificationToken);

    @Query("select u from User u order by u.totalScore desc")
    List<User> findTopUsersByScore(Pageable pageable);
}
