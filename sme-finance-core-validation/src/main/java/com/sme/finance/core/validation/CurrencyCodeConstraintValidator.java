package com.sme.finance.core.validation;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class CurrencyCodeConstraintValidator implements ConstraintValidator<CurrencyCode, String> {

    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^\\d{3}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return CURRENCY_CODE_PATTERN.matcher(value).matches();
    }
}
