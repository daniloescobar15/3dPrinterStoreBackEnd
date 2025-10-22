package com.printerstore.backend.application.authentication.usecase;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.printerstore.backend.domain.authentication.gateway.AuthenticationGateway;
import com.printerstore.backend.domain.authentication.gateway.dto.AuthenticationRequestDto;
import com.printerstore.backend.domain.authentication.gateway.dto.AuthenticationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAuthenticationTokenUseCase {

    private final AuthenticationGateway authenticationGateway;
    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    /**
     * Authenticates a user against the authentication system
     */
    private AuthenticationResponseDto authenticate(String username, String password) {
        log.info("Starting authentication for user: {}", username);
        
        AuthenticationRequestDto request = new AuthenticationRequestDto(username, password);
        AuthenticationResponseDto response = authenticationGateway.authenticate(request);
        
        if (response != null && response.getResponseCode() == 200) {
            log.info("Authentication successful for user: {}", username);
            return response;
        } else {
            log.warn("Authentication failed for user: {}. Code: {}", 
                    username, 
                    response != null ? response.getResponseCode() : "unknown");
            throw new RuntimeException("Authentication failed: " + 
                    (response != null ? response.getResponseMessage() : "null response"));
        }
    }

    /**
     * Validates if a cached token is still valid by checking its expiration date
     */
    private boolean isTokenValid(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Instant expiresAt = decodedJWT.getExpiresAtAsInstant();
            
            if (expiresAt == null) {
                log.warn("Token does not have an expiration date");
                return false;
            }
            
            boolean isValid = expiresAt.isAfter(Instant.now());
            if (!isValid) {
                log.debug("Token has expired");
            }
            return isValid;
        } catch (Exception e) {
            log.warn("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets the authentication token from cache if valid, 
     * otherwise authenticates and saves it in cache
     */
    public String execute(String username, String password) {
        String cachedToken = tokenCache.get(username);
        if (cachedToken != null && isTokenValid(cachedToken)) {
            log.debug("Token retrieved from cache for user: {}", username);
            return cachedToken;
        }
        
        log.debug("Token not found in cache or expired. Authenticating user: {}", username);
        AuthenticationResponseDto response = authenticate(username, password);
        String token = response.getData().getToken();
        
        tokenCache.put(username, token);
        log.debug("Token saved in cache for user: {}", username);
        
        return token;
    }
}