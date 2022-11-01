package com.sme.finance.core.error;

import lombok.experimental.UtilityClass;

import java.net.URI;

@UtilityClass
public class ErrorConstants {

    public static final String ERR_SERVER = "error.server";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String PROBLEM_BASE_URL = "https://www.finance.sem.com";

    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/error");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
}
