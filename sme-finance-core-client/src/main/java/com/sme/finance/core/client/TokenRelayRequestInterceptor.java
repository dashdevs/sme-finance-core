package com.sme.finance.core.client;

import com.sme.finance.core.security.oauth2.AuthorizationHeaderUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class TokenRelayRequestInterceptor implements RequestInterceptor {

    public static final String AUTHORIZATION = "Authorization";

    private final AuthorizationHeaderUtil authorizationHeaderUtil;

    public TokenRelayRequestInterceptor(final AuthorizationHeaderUtil authorizationHeaderUtil) {
        super();
        this.authorizationHeaderUtil = authorizationHeaderUtil;
    }

    @Override
    public void apply(final RequestTemplate template) {
        authorizationHeaderUtil.getAuthorizationHeader()
            .ifPresent(header -> template.header(AUTHORIZATION, header));
    }
}
