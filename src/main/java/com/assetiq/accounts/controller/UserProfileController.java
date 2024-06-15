package com.assetiq.accounts.controller;

import com.assetiq.accounts.model.UserProfile;
import com.assetiq.accounts.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.HeaderParam;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping("/user-profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putUserProfile(@RequestHeader("X-User-Id") String userId, @RequestBody UserProfile userProfile) {
        userProfileService.putUserProfile(userId, userProfile);
    }
}
