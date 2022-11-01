package com.sme.finance.core.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Constraint(validatedBy = CurrencyCodeConstraintValidator.class)
public @interface CurrencyCode {

    String message() default "Currency code must follow 3-digit ISO 4217 format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
