package com.printerstore.backend.infrastructure.adapter.in.web;

import com.printerstore.backend.configuration.webclient.FusionAuthProperties;
import com.printerstore.backend.infrastructure.adapter.in.AuthenticationServiceAdapter;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client.FusionAuthApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginRequest;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller para operaciones de autenticación
 * Adapta entrada HTTP al puerto de autenticación del dominio
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final FusionAuthApiClient fusionAuthApiClient;
    private final FusionAuthProperties fusionAuthProperties;

    /**
     * Endpoint de login
     *
     * @param loginRequest solicitud con usuario y contraseña
     * @return respuesta con token y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<FusionAuthLoginResponse>  login(@RequestBody LoginRequest loginRequest) {
        log.info("Solicitud de login recibida para usuario: {}", loginRequest.getLoginId());
        
        try {
            Map<String, Object> metaData = new HashMap<>();
            Map<String, String> device = new HashMap<>();
            device.put("description", "web");
            metaData.put("device", device);

            FusionAuthLoginRequest request = FusionAuthLoginRequest.builder()
                    .applicationId(fusionAuthProperties.getApplicationId())
                    .loginId(loginRequest.getLoginId())
                    .password(loginRequest.password)
                    .metaData(metaData)
                    .build();

            return ResponseEntity.ok(fusionAuthApiClient.login(request));

        } catch (Exception e) {
            log.error("Error en login para usuario: {}", loginRequest.getLoginId(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Modelo para la solicitud de login
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LoginRequest {
        private String loginId;
        private String password;
    }

    /**
     * Modelo para la respuesta de autenticación
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthenticationResponse {
        private String token;
    }

    /**
     * Modelo para respuesta de error
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }
}