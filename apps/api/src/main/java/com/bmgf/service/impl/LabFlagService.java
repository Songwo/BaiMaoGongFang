package com.bmgf.service.impl;

import com.bmgf.po.Challenge;
import com.bmgf.po.LabFlag;
import com.bmgf.dao.LabFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
    @Service
    public class LabFlagService {

        @Autowired
        private ChallengeService challengeService;

        public boolean checkFlag(String imageName, String inputFlag) {
            Optional<Challenge> opt = challengeService.findByImageName(imageName);
            if (opt.isEmpty()) {
                System.out.println("未找到镜像名: " + imageName);
                return false;
            }
            Challenge challenge = opt.get();
            String cleanedFlag = extractFlagContent(challenge.getFlag());
            System.out.println("数据库flag: " + challenge.getFlag() + "，提取后: " + cleanedFlag);
            return inputFlag.equals(cleanedFlag);
        }

        private String extractFlagContent(String dbFlag) {
            Pattern pattern = Pattern.compile("flag\\{(.*?)}");
            Matcher matcher = pattern.matcher(dbFlag);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return dbFlag; // 如果格式不符就返回原值
        }
    }
