package com.bmgf.service.impl;

import com.bmgf.po.Reward;
import com.bmgf.dao.RewardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RewardService {

    @Autowired
    private RewardRepository rewardRepository;

    public List<Reward> getReward() {
        return rewardRepository.findAll();
    }

    public Reward findById(String id) {
        if(rewardRepository.findById(id).isEmpty())
            return null;
        return rewardRepository.findById(id).get();
    }

    public Reward save(Reward reward) {
        return rewardRepository.save(reward);
    }


}
