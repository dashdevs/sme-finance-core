package com.sme.finance.core.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomValidationConfiguration {

    @Bean
    public CurrencyCodeConstraintValidator currencyCodeConstraintValidator() {
        return new CurrencyCodeConstraintValidator();
    }
}
