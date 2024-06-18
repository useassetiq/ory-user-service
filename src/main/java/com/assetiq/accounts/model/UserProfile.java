package com.assetiq.accounts.model;

import com.assetiq.accounts.validator.GenderValidation;
import com.assetiq.accounts.validator.ValidAge;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record UserProfile(
        String username,
        @Valid Gender gender,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") @ValidAge LocalDate dateOfBirth,
        List<@Valid PhoneNumber> phoneNumbers,
        List<@Valid Address> addresses) {

    public record PhoneNumber(
            @NotBlank String countryCode, @NotBlank String number, String extension, @NotNull PhoneNumberType type) {

        enum PhoneNumberType {
            HOME,
            WORK,
            MOBILE
        }
    }

    public record Address(
            @NotBlank String addressLine1,
            String addressLine2,
            String addressLine3,
            @NotBlank String city,
            @NotBlank String province,
            @NotBlank String country,
            @NotBlank String zipCode,
            @Valid GeoLocation geoLocation) {

        public record GeoLocation(@NotNull Double latitude, @NotNull Double longitude) {}
    }

    @GenderValidation
    public record Gender(@NotNull GenderType type, String description) {

        public enum GenderType {
            MALE,
            FEMALE,
            OTHER
        }
    }
}
