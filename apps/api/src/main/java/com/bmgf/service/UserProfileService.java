package com.bmgf.service;

import com.bmgf.po.UserProfile;

public interface UserProfileService {
    UserProfile getUserProfile(String userId);
    UserProfile saveUserProfile(UserProfile userProfile);
}