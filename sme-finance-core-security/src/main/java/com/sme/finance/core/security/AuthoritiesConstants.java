package com.sme.finance.core.security;

import lombok.experimental.UtilityClass;

/**
 * Constants for Spring Security authorities.
 */
@UtilityClass
public class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";
}
