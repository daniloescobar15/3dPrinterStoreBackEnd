package com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client;

import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginRequest;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginResponse;

public interface FusionAuthApiClient {
    
    /**
     * Performs login in FusionAuth
     * 
     * @param loginRequest request with loginId and password
     * @return response with token and user data
     */
    FusionAuthLoginResponse login(FusionAuthLoginRequest loginRequest);

    /**
     * Validates a JWT token in FusionAuth
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Extracts the userId from the JWT token
     *
     * @param token the JWT token
     * @return the userId from the token, or null if it cannot be extracted
     */
    String extractUserIdFromToken(String token);

}