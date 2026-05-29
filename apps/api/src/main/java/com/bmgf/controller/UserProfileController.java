package com.bmgf.controller;

import com.bmgf.po.UserProfile;
import com.bmgf.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String userId) {
        UserProfile profile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/profile")
    public ResponseEntity<UserProfile> createUserProfile(@RequestBody UserProfile userProfile) {
        UserProfile savedProfile = userProfileService.saveUserProfile(userProfile);
        System.out.println("344444444444444444444444"+savedProfile);
        return ResponseEntity.ok(savedProfile);
    }
}
