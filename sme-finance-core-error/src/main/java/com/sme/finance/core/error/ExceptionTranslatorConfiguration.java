package com.sme.finance.core.error;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ExceptionTranslatorConfiguration {

    @Bean
    public ExceptionTranslator exceptionTranslator(Environment environment) {
        return new ExceptionTranslator(environment);
    }
}
