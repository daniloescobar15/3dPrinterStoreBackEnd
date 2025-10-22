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
            
            log.info("Enviando solicitud de login a FusionAuth: {}", url);
            log.debug("Login ID: {}", loginRequest.getLoginId());
            
            FusionAuthLoginResponse response = restTemplate.postForObject(
                    url,
                    request,
                    FusionAuthLoginResponse.class
            );
            
            if (response != null && response.getToken() != null) {
                log.info("Login exitoso en FusionAuth. Usuario: {}, ID: {}", 
                        response.getUser().getUsername(),
                        response.getUser().getId());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al realizar login en FusionAuth", e);
            throw new RuntimeException("Error de login en FusionAuth: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                log.warn("Token vacío o nulo");
                return false;
            }

            log.debug("Validando token JWT usando JWKS");
            boolean isValid = jwtValidator.validateToken(token);
            
            if (isValid) {
                log.debug("Token JWT válido");
            } else {
                log.warn("Token JWT inválido o expirado");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.warn("Error al validar token JWT: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String extractUserIdFromToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                log.warn("Token vacío o nulo");
                return null;
            }

            var decodedJWT = jwtValidator.getDecodedToken(token);
            if (decodedJWT == null) {
                log.warn("No se pudo decodificar el token JWT");
                return null;
            }

            String userId = decodedJWT.getSubject();
            if (userId != null && !userId.isEmpty()) {
                log.debug("UserId extraído del token: {}", userId);
                return userId;
            }

            var claims = decodedJWT.getClaims();
            if (claims.containsKey("user_id")) {
                userId = claims.get("user_id").asString();
                log.debug("UserId extraído del claim user_id: {}", userId);
                return userId;
            }

            if (claims.containsKey("uid")) {
                userId = claims.get("uid").asString();
                log.debug("UserId extraído del claim uid: {}", userId);
                return userId;
            }

            log.warn("No se pudo extraer el userId del token JWT");
            return null;

        } catch (Exception e) {
            log.warn("Error al extraer userId del token JWT: {}", e.getMessage());
            return null;
        }
    }

}