package com.assetiq.accounts.service;

import com.assetiq.accounts.error.Failure;
import com.assetiq.accounts.model.UserProfile;

import java.util.Optional;


public interface UserProfileService {

    Optional<Failure> putUserProfile(String userId, UserProfile userProfile);
}
