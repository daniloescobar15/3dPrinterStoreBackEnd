package com.printerstore.backend.core.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.client.PuntoRedApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PuntoRedAuthenticationService {

    private final PuntoRedApiClient puntoRedApiClient;
    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    /**
     * Autentica un usuario contra el sistema Punto Red
     * 
     * @param username el nombre de usuario
     * @param password la contraseña del usuario
     * @return respuesta de autenticación con token
     */
    public AuthenticationResponse authenticate(String username, String password) {
        log.info("Iniciando autenticación para usuario: {}", username);
        
        AuthenticationRequest request = new AuthenticationRequest(username, password);
        AuthenticationResponse response = puntoRedApiClient.authenticate(request);
        
        if (response != null && response.getResponseCode() == 200) {
            log.info("Autenticación exitosa para usuario: {}", username);
            return response;
        } else {
            log.warn("Autenticación fallida para usuario: {}. Código: {}", 
                    username, 
                    response != null ? response.getResponseCode() : "desconocido");
            throw new RuntimeException("Autenticación fallida: " + 
                    (response != null ? response.getResponseMessage() : "respuesta nula"));
        }
    }

    /**
     * Valida si un token en cache sigue siendo válido verificando su fecha de expiración
     * 
     * @param token el token a validar
     * @return true si el token es válido y no ha expirado, false en caso contrario
     */
    private boolean isTokenValid(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Instant expiresAt = decodedJWT.getExpiresAtAsInstant();
            
            if (expiresAt == null) {
                log.warn("Token no tiene fecha de expiración");
                return false;
            }
            
            boolean isValid = expiresAt.isAfter(Instant.now());
            if (!isValid) {
                log.debug("Token ha expirado");
            }
            return isValid;
        } catch (Exception e) {
            log.warn("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el token de autenticación desde cache si es válido, 
     * sino autentica y lo guarda en cache
     * 
     * @param username el nombre de usuario
     * @param password la contraseña del usuario
     * @return el token JWT
     */
    public String getAuthenticationToken(String username, String password) {
        String cachedToken = tokenCache.get(username);
        if (cachedToken != null && isTokenValid(cachedToken)) {
            log.debug("Token obtenido del cache para usuario: {}", username);
            return cachedToken;
        }
        
        log.debug("Token no encontrado en cache o expirado. Autenticando usuario: {}", username);
        AuthenticationResponse response = authenticate(username, password);
        String token = response.getData().getToken();
        
        tokenCache.put(username, token);
        log.debug("Token guardado en cache para usuario: {}", username);
        
        return token;
    }
}