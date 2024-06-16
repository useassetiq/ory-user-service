package com.assetiq.accounts.controller;

import com.assetiq.accounts.error.FailureCodeMapper;
import com.assetiq.accounts.model.UserProfile;
import com.assetiq.accounts.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping("/user-profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> putUserProfile(
            @RequestHeader("X-User-Id") String userId, @RequestBody UserProfile userProfile) {
        return userProfileService
                .putUserProfile(userId, userProfile)
                .map(failure -> ResponseEntity.status(FailureCodeMapper.fromCode(failure.code()))
                        .body(failure))
                .orElse(ResponseEntity.noContent().build());
    }
}
