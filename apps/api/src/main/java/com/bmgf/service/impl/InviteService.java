package com.bmgf.service.impl;

import com.bmgf.dao.InviteCodeRepository;
import com.bmgf.po.InviteCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class InviteService {
    @Autowired
    private InviteCodeRepository inviteCodeRepository;

    @Autowired
    private UserService userService;

    // 生成邀请码
    public String generateInviteCode(String username) {
        // 检查是否已有未使用的邀请码
        Optional<InviteCode> existingCode = inviteCodeRepository.findByInviterAndUsed(username, false);
        if (existingCode.isPresent()) {
            return existingCode.get().getCode();
        }

        // 生成新的邀请码
        String code = generateUniqueCode();
        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode(code);
        inviteCode.setInviter(username);
        inviteCode.setCreatedAt(LocalDateTime.now());
        inviteCode.setUsed(false);

        inviteCodeRepository.save(inviteCode);
        return code;
    }

    // 验证并使用邀请码
    public boolean verifyAndUseInviteCode(String code, String invitee) {
        // 检查是否已被邀请过
        if (inviteCodeRepository.existsByInvitee(invitee)) {
            throw new RuntimeException("该用户已被邀请过");
        }

        Optional<InviteCode> inviteCodeOpt = inviteCodeRepository.findByCode(code);
        if (inviteCodeOpt.isEmpty()) {
            throw new RuntimeException("无效的邀请码");
        }

        InviteCode inviteCode = inviteCodeOpt.get();
        if (inviteCode.isUsed()) {
            throw new RuntimeException("该邀请码已被使用");
        }

        // 更新邀请码状态
        inviteCode.setUsed(true);
        inviteCode.setInvitee(invitee);
        inviteCode.setUsedAt(LocalDateTime.now());
        inviteCodeRepository.save(inviteCode);


        // 给邀请人发放奖励
        userService.addPoints(inviteCode.getInviter(), 50); // 假设奖励50积分

        return true;
    }

    // 生成唯一邀请码
    private String generateUniqueCode() {
        String code;
        do {
            // 生成6位随机字符串
            code = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        } while (inviteCodeRepository.findByCode(code).isPresent());
        return code;
    }
}