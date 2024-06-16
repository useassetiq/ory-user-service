package com.assetiq.accounts.validator;

import com.assetiq.accounts.model.UserProfile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class GenderValidator implements ConstraintValidator<GenderValidation, UserProfile.Gender> {

    @Override
    public void initialize(GenderValidation constraintAnnotation) {}

    @Override
    public boolean isValid(UserProfile.Gender gender, ConstraintValidatorContext context) {
        if (gender == null || gender.type() == null) {
            return true;
        }

        if (gender.type() == UserProfile.Gender.GenderType.OTHER) {
            return StringUtils.isNotBlank(gender.description());
        }

        return true;
    }
}
