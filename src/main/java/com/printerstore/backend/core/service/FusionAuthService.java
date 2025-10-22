package com.printerstore.backend.core.service;

import com.printerstore.backend.configuration.webclient.FusionAuthProperties;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client.FusionAuthApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginRequest;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model.FusionAuthLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FusionAuthService {

    private final FusionAuthApiClient fusionAuthApiClient;
    private final FusionAuthProperties fusionAuthProperties;

    /**
     * Realiza login con usuario y contraseña
     * 
     * @param loginId email o nombre de usuario
     * @param password contraseña del usuario
     * @return respuesta de login con token y datos del usuario
     */
    public FusionAuthLoginResponse login(String loginId, String password) {
        log.info("Iniciando login para usuario: {}", loginId);
        
        Map<String, Object> metaData = new HashMap<>();
        Map<String, String> device = new HashMap<>();
        device.put("description", "web");
        metaData.put("device", device);
        
        FusionAuthLoginRequest loginRequest = FusionAuthLoginRequest.builder()
                .applicationId(fusionAuthProperties.getApplicationId())
                .loginId(loginId)
                .password(password)
                .metaData(metaData)
                .build();
        
        FusionAuthLoginResponse response = fusionAuthApiClient.login(loginRequest);
        
        if (response == null || response.getToken() == null) {
            log.error("Login fallido para usuario: {}. Respuesta nula o sin token", loginId);
            throw new RuntimeException("Login fallido: Respuesta inválida de FusionAuth");
        }
        
        log.info("Login exitoso para usuario: {}", loginId);
        return response;
    }
}