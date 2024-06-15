package com.assetiq.accounts.service;

import com.assetiq.accounts.error.Failure;
import com.assetiq.accounts.model.UserProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sh.ory.kratos.ApiResponse;
import sh.ory.kratos.api.IdentityApi;
import sh.ory.kratos.model.Identity;
import sh.ory.kratos.model.UpdateIdentityBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
    private final IdentityApi identityApi;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Failure> putUserProfile(String userId, UserProfile userProfile) {
        return Try.of(() -> identityApi.getIdentityWithHttpInfo(userId, List.of()))
                .onFailure(throwable -> log.error("Failed to get identity with id {}", userId, throwable))
                .toEither()
                .fold(
                        throwable -> Optional.of(new Failure("Failed to get identity with id " + userId, "error_getting_identity", throwable)),
                        identity -> updateIdentity(userId, identity, userProfile));
    }

    private Optional<Failure> updateIdentity(String userId, ApiResponse<Identity> response, UserProfile userProfile) {
        if (response.getStatusCode() == 404) {
            return Optional.of( new Failure("Identity with id %s not found".formatted(userId), "identity_not_found", null));
        }
        Identity identity = response.getData();

        Map<String, Object> publicMetadata = mergeMaps(objectMapper.convertValue(userProfile, new TypeReference<>() {}), objectMapper.convertValue(identity, new TypeReference<>() {}));
        UpdateIdentityBody updateIdentityBody =   new UpdateIdentityBody();
        updateIdentityBody.setMetadataPublic(publicMetadata);
        updateIdentityBody.setTraits(identity.getTraits());
        updateIdentityBody.setSchemaId(identity.getSchemaId());
        updateIdentityBody.setState(UpdateIdentityBody.StateEnum.fromValue(identity.getState().getValue()));
        return Try.of(() -> identityApi.updateIdentity(identity.getId(), updateIdentityBody))
                .onFailure(throwable -> log.error("Failed to update identity with id {}", identity.getId(), throwable))
                .toEither()
                .mapLeft(throwable -> new Failure("Failed to update identity with id " + identity.getId(), "error_updating_identity", throwable))
                .fold(Optional::of, ignored -> Optional.empty());
    }

    public static Map<String, Object> mergeMaps(Map<String, Object> existingIdentity, Map<String, Object> newInformation) {
        Map<String, Object> mergedMap = new HashMap<>(existingIdentity);

        for (Map.Entry<String, Object> entry : newInformation.entrySet()) {
            if (entry.getValue() != null) {
                mergedMap.put(entry.getKey(), entry.getValue());
            }
        }

        return mergedMap;
    }
}
