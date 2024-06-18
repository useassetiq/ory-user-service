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
import sh.ory.kratos.model.VerifiableIdentityAddress;

import static com.assetiq.accounts.error.FailureCodeMapper.error_getting_identity;
import static com.assetiq.accounts.error.FailureCodeMapper.identity_inactive;
import static com.assetiq.accounts.error.FailureCodeMapper.identity_not_found;
import static com.assetiq.accounts.error.FailureCodeMapper.error_updating_identity;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
    private final IdentityApi identityApi;
    private final ObjectMapper objectMapper;
    private static final String VERIFIED = "completed";

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
                return new Failure("Identity with id '%s' not found".formatted(userId), identity_not_found.getCode(), null);
            }
        }

        return new Failure("Failed to get identity with id '%s'".formatted(userId), error_getting_identity.getCode(), throwable);
    }

    private Optional<Failure> updateIdentity(Identity identity, UserProfile userProfile) {

        if (identity.getState() == Identity.StateEnum.INACTIVE) {
            return Optional.of(new Failure("Identity with id %s is inactive".formatted(identity.getId()), identity_inactive.getCode(), null));
        }

        long verifiedAddressCount = identity.getVerifiableAddresses()
                .stream()
                .filter(VerifiableIdentityAddress::getVerified)
                .filter(verifiableIdentityAddress -> VERIFIED.equals(verifiableIdentityAddress.getStatus()))
                .count();

        if (verifiedAddressCount == 0L) {
            return Optional.of(new Failure("Identity with id %s has no verified addresses".formatted(identity.getId()), identity_inactive.getCode(), null));
        }

        Map<String, Object> publicMetadata = toMap(identity, userProfile);

        if (publicMetadata.isEmpty()) {
            return Optional.empty();
        }

        UpdateIdentityBody updateIdentityBody = new UpdateIdentityBody();
        updateIdentityBody.setMetadataPublic(publicMetadata);
        updateIdentityBody.setTraits(identity.getTraits());
        updateIdentityBody.setSchemaId(identity.getSchemaId());
        updateIdentityBody.setState(UpdateIdentityBody.StateEnum.fromValue(identity.getState().getValue()));

        return Try.of(() -> identityApi.updateIdentity(identity.getId(), updateIdentityBody))
                .onFailure(throwable -> log.error("Failed to update identity with id {}", identity.getId(), throwable))
                .toEither()
                .mapLeft(throwable -> new Failure(
                        "Failed to update identity with id %s".formatted(identity.getId()), error_updating_identity.getCode(), throwable))
                .fold(Optional::of, ignored -> Optional.empty());
    }

    public Map<String, Object> toMap(Identity identity, UserProfile userProfile) {
        Map<String, Object> toUpdate = new HashMap<>();
        Optional.ofNullable(identity.getMetadataPublic())
            .ifPresent(metaPublic -> toUpdate.putAll(objectMapper.convertValue(metaPublic, new TypeReference<Map<String, Object>>() {})));
        Map<String, Object> newProfile = objectMapper.convertValue(userProfile, new TypeReference<>() {});

        for (Map.Entry<String, Object> entry : newProfile.entrySet()) {
            if (entry.getValue() != null) {
                toUpdate.put(entry.getKey(), entry.getValue());
            }
        }

        return toUpdate;

    }
}
