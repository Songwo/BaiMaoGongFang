package com.bmgf.dao;

import com.bmgf.po.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, String> {
    Optional<InviteCode> findByCode(String code);

    Optional<InviteCode> findByInviterAndUsed(String inviter, boolean used);

    boolean existsByInvitee(String invitee);
}
