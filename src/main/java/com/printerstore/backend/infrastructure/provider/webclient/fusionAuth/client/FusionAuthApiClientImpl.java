package com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client;

import com.printerstore.backend.configuration.webclient.FusionAuthProperties;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginRequest;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FusionAuthApiClientImpl implements FusionAuthApiClient {

    private static final String LOGIN_ENDPOINT = "/api/login";

    private final RestTemplate restTemplate;
    private final FusionAuthProperties fusionAuthProperties;
    private final JwtValidator jwtValidator;

    @Override
    public FusionAuthLoginResponse login(FusionAuthLoginRequest loginRequest) {
        try {
            String url = fusionAuthProperties.getUrl() + LOGIN_ENDPOINT;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", fusionAuthProperties.getAuthorization());
            
            HttpEntity<FusionAuthLoginRequest> request = new HttpEntity<>(loginRequest, headers);
            
            log.info("Sending login request to FusionAuth: {}", url);
            log.debug("Login ID: {}", loginRequest.getLoginId());
            
            FusionAuthLoginResponse response = restTemplate.postForObject(
                    url,
                    request,
                    FusionAuthLoginResponse.class
            );
            
            if (response != null && response.getToken() != null) {
                log.info("Login successful in FusionAuth. User: {}, ID: {}", 
                        response.getUser().getUsername(),
                        response.getUser().getId());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error performing login in FusionAuth", e);
            throw new RuntimeException("Login error in FusionAuth: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                log.warn("Token empty or null");
                return false;
            }

            log.debug("Validating JWT token using JWKS");
            boolean isValid = jwtValidator.validateToken(token);
            
            if (isValid) {
                log.debug("JWT token valid");
            } else {
                log.warn("JWT token invalid or expired");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.warn("Error validating JWT token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String extractUserIdFromToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                log.warn("Token empty or null");
                return null;
            }

            var decodedJWT = jwtValidator.getDecodedToken(token);
            if (decodedJWT == null) {
                log.warn("Could not decode JWT token");
                return null;
            }

            String userId = decodedJWT.getSubject();
            if (userId != null && !userId.isEmpty()) {
                log.debug("UserId extracted from token: {}", userId);
                return userId;
            }

            var claims = decodedJWT.getClaims();
            if (claims.containsKey("user_id")) {
                userId = claims.get("user_id").asString();
                log.debug("UserId extracted from user_id claim: {}", userId);
                return userId;
            }

            if (claims.containsKey("uid")) {
                userId = claims.get("uid").asString();
                log.debug("UserId extracted from uid claim: {}", userId);
                return userId;
            }

            log.warn("Could not extract userId from JWT token");
            return null;

        } catch (Exception e) {
            log.warn("Error extracting userId from JWT token: {}", e.getMessage());
            return null;
        }
    }

}