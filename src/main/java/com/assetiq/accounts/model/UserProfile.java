package com.assetiq.accounts.model;

import com.assetiq.accounts.validator.GenderValidation;
import com.assetiq.accounts.validator.ValidAge;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Valid
public record UserProfile(
        String username,
        @GenderValidation Gender gender,
        @ValidAge LocalDate dateOfBirth,
        List<@NotEmpty PhoneNumber> phoneNumber,
        Address address) {

    @Valid
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
            @NotBlank String zipCode) {}

    @Valid
    public record Gender(@NotNull GenderType type, String description) {

        public enum GenderType {
            MALE,
            FEMALE,
            OTHER
        }
    }
}
