package com.printerstore.backend.infrastructure.entrypoint.controller;

import com.printerstore.backend.core.service.FusionAuthService;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FusionAuthController {

    private final FusionAuthService fusionAuthService;

    /**
     * Endpoint de login
     * 
     * @param loginRequest solicitud con usuario y contraseña
     * @return respuesta con token y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<FusionAuthLoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Solicitud de login recibida para usuario: {}", loginRequest.getLoginId());
        
        try {
            FusionAuthLoginResponse response = fusionAuthService.login(
                    loginRequest.getLoginId(),
                    loginRequest.getPassword()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en login para usuario: {}", loginRequest.getLoginId(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
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
}