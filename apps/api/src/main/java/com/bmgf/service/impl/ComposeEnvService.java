package com.bmgf.service.impl;

import com.bmgf.po.ComposeEnvironment;
import com.bmgf.dao.ComposeEnvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComposeEnvService {

    @Autowired
    private ComposeEnvRepository composeEnvRepository;

    public ComposeEnvironment findByUserId(String userId) {
        return composeEnvRepository.findByUserId(userId);
    }
}

