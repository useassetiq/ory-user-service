package com.assetiq.accounts.service;

import com.assetiq.accounts.error.Failure;
import com.assetiq.accounts.model.UserProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sh.ory.kratos.ApiException;
import sh.ory.kratos.api.IdentityApi;
import sh.ory.kratos.model.Identity;
import sh.ory.kratos.model.UpdateIdentityBody;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
    private final IdentityApi identityApi;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Failure> putUserProfile(String userId, UserProfile userProfile) {
        return Try.of(() -> identityApi.getIdentity(userId, List.of()))
                .onFailure(throwable -> log.error("Failed to get identity with id {}", userId, throwable))
                .toEither()
                .mapLeft(throwable -> mapFailure(userId, throwable))
                .fold(Optional::of, identity -> updateIdentity(identity, userProfile));
    }

    private Failure mapFailure(String userId, Throwable throwable) {
        if (throwable instanceof ApiException exception) {
            if (exception.getCode() == 404) {
                return new Failure("Identity with id '" + userId + "' not found", "identity_not_found", null);
            }
        }

        return new Failure("Failed to get identity with id " + userId, "error_getting_identity", throwable);
    }

    private Optional<Failure> updateIdentity(Identity identity, UserProfile userProfile) {

        Map<String, Object> publicMetadata = mergeMaps(
                objectMapper.convertValue(userProfile, new TypeReference<>() {}),
                objectMapper.convertValue(identity, new TypeReference<>() {}));
        UpdateIdentityBody updateIdentityBody = new UpdateIdentityBody();
        updateIdentityBody.setMetadataPublic(publicMetadata);
        updateIdentityBody.setTraits(identity.getTraits());
        updateIdentityBody.setSchemaId(identity.getSchemaId());
        updateIdentityBody.setState(
                UpdateIdentityBody.StateEnum.fromValue(identity.getState().getValue()));
        return Try.of(() -> identityApi.updateIdentity(identity.getId(), updateIdentityBody))
                .onFailure(throwable -> log.error("Failed to update identity with id {}", identity.getId(), throwable))
                .toEither()
                .mapLeft(throwable -> new Failure(
                        "Failed to update identity with id " + identity.getId(), "error_updating_identity", throwable))
                .fold(Optional::of, ignored -> Optional.empty());
    }

    public static Map<String, Object> mergeMaps(
            Map<String, Object> existingIdentity, Map<String, Object> newInformation) {
        Map<String, Object> mergedMap = new HashMap<>(existingIdentity);

        for (Map.Entry<String, Object> entry : newInformation.entrySet()) {
            if (entry.getValue() != null) {
                mergedMap.put(entry.getKey(), entry.getValue());
            }
        }

        return mergedMap;
    }
}
