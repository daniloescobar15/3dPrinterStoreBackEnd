package com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client;

import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginRequest;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginResponse;

public interface FusionAuthApiClient {
    
    /**
     * Realiza login en FusionAuth
     * 
     * @param loginRequest solicitud con loginId y password
     * @return respuesta con token y datos del usuario
     */
    FusionAuthLoginResponse login(FusionAuthLoginRequest loginRequest);

    /**
     * Valida un token JWT en FusionAuth
     *
     * @param token el token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    boolean validateToken(String token);

    /**
     * Extrae el userId del token JWT
     *
     * @param token el token JWT
     * @return el userId del token, o null si no puede extraerse
     */
    String extractUserIdFromToken(String token);

}