package com.printerstore.backend.infrastructure.adapter.in.web;

import com.printerstore.backend.infrastructure.adapter.in.AuthenticationServiceAdapter;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller para operaciones de autenticación
 * Adapta entrada HTTP al puerto de autenticación del dominio
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationRestController {

    private final AuthenticationServiceAdapter authenticationServiceAdapter;

    /**
     * Endpoint de login
     * 
     * @param loginRequest solicitud con usuario y contraseña
     * @return respuesta con token y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Solicitud de login recibida para usuario: {}", loginRequest.getLoginId());
        
        try {
            String token = authenticationServiceAdapter.getAuthenticationToken(
                    loginRequest.getLoginId(),
                    loginRequest.getPassword()
            );
            
            // Retornar el token en la respuesta
            return ResponseEntity.ok(new AuthenticationResponse(token));
            
        } catch (Exception e) {
            log.error("Error en login para usuario: {}", loginRequest.getLoginId(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Credenciales inválidas"));
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