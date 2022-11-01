package com.sme.finance.core.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationHeaderUtil {

    private final RestTemplateBuilder restTemplateBuilder;
    private final OAuth2AuthorizedClientService clientService;

    public Optional<String> getAuthorizationHeader() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            final String name = oauthToken.getName();
            final String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            final OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(registrationId, name);

            if (null == client) {
                throw new OAuth2AuthorizationException(new OAuth2Error("access_denied", "The token is expired", null));
            }

            final OAuth2AccessToken accessToken = client.getAccessToken();

            if (accessToken != null) {
                final String tokenType = accessToken.getTokenType().getValue();

                String accessTokenValue = accessToken.getTokenValue();
                if (isExpired(accessToken)) {
                    log.info("AccessToken expired, refreshing automatically");
                    accessTokenValue = refreshToken(client, oauthToken);

                    if (null == accessTokenValue) {
                        SecurityContextHolder.getContext().setAuthentication(null);

                        throw new OAuth2AuthorizationException(new OAuth2Error("access_denied", "The token is expired", null));
                    }
                }

                String authorizationHeaderValue = String.format("%s %s", tokenType, accessTokenValue);
                return Optional.of(authorizationHeaderValue);
            }
        } else if (authentication instanceof JwtAuthenticationToken accessToken) {
            final String tokenValue = accessToken.getToken().getTokenValue();
            final String authorizationHeaderValue = String.format("%s %s", OAuth2AccessToken.TokenType.BEARER.getValue(), tokenValue);

            return Optional.of(authorizationHeaderValue);
        }

        return Optional.empty();
    }

    private String refreshToken(OAuth2AuthorizedClient client, OAuth2AuthenticationToken oauthToken) {
        final OAuth2AccessTokenResponse atr = refreshTokenClient(client);
        if (atr.getAccessToken() == null) {
            log.info("Failed to refresh token for user");
            return null;
        }

        final OAuth2RefreshToken refreshToken = atr.getRefreshToken() != null ? atr.getRefreshToken() : client.getRefreshToken();
        final OAuth2AuthorizedClient updatedClient = new OAuth2AuthorizedClient(
            client.getClientRegistration(),
            client.getPrincipalName(),
            atr.getAccessToken(),
            refreshToken
        );

        clientService.saveAuthorizedClient(updatedClient, oauthToken);
        return atr.getAccessToken().getTokenValue();
    }

    private OAuth2AccessTokenResponse refreshTokenClient(OAuth2AuthorizedClient currentClient) {
        final MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.REFRESH_TOKEN.getValue());
        formParameters.add(OAuth2ParameterNames.REFRESH_TOKEN, requireNonNull(currentClient.getRefreshToken()).getTokenValue());
        formParameters.add(OAuth2ParameterNames.CLIENT_ID, currentClient.getClientRegistration().getClientId());

        final RequestEntity<MultiValueMap<String, String>> requestEntity =
            RequestEntity
                .post(URI.create(currentClient.getClientRegistration().getProviderDetails().getTokenUri()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formParameters);

        try {
            final RestTemplate r = restTemplate(
                currentClient.getClientRegistration().getClientId(),
                currentClient.getClientRegistration().getClientSecret()
            );

            final ResponseEntity<OAuthIdpTokenResponseDTO> responseEntity = r.exchange(requestEntity, OAuthIdpTokenResponseDTO.class);
            final OAuthIdpTokenResponseDTO responseBody = requireNonNull(responseEntity.getBody());

            return toOAuth2AccessTokenResponse(responseBody);
        } catch (OAuth2AuthorizationException e) {
            log.error("Unable to refresh token", e);
            throw new OAuth2AuthenticationException(e.getError(), e);
        }
    }

    private OAuth2AccessTokenResponse toOAuth2AccessTokenResponse(OAuthIdpTokenResponseDTO oAuthIdpResponse) {
        final Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("id_token", oAuthIdpResponse.getIdToken());
        additionalParameters.put("not-before-policy", oAuthIdpResponse.getNotBefore());
        additionalParameters.put("refresh_expires_in", oAuthIdpResponse.getRefreshExpiresIn());
        additionalParameters.put("session_state", oAuthIdpResponse.getSessionState());

        return OAuth2AccessTokenResponse
            .withToken(oAuthIdpResponse.getAccessToken())
            .expiresIn(oAuthIdpResponse.getExpiresIn())
            .refreshToken(oAuthIdpResponse.getRefreshToken())
            .scopes(Pattern.compile("\\s").splitAsStream(oAuthIdpResponse.getScope()).collect(Collectors.toSet()))
            .tokenType(OAuth2AccessToken.TokenType.BEARER)
            .additionalParameters(additionalParameters)
            .build();
    }

    private RestTemplate restTemplate(String clientId, String clientSecret) {
        return restTemplateBuilder
            .additionalMessageConverters(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter())
            .errorHandler(new OAuth2ErrorResponseErrorHandler())
            .basicAuthentication(clientId, clientSecret)
            .build();
    }

    private boolean isExpired(final OAuth2AccessToken accessToken) {
        final Instant now = Instant.now();
        final Instant expiresAt = requireNonNull(accessToken.getExpiresAt());

        return now.isAfter(expiresAt.minus(Duration.ofMinutes(1L)));
    }
}
