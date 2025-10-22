package com.printerstore.backend.infrastructure.adapter.in.web;

import com.printerstore.backend.configuration.webclient.FusionAuthProperties;
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
 * REST Controller for authentication operations
 * Adapts HTTP input to the domain's authentication port
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final FusionAuthApiClient fusionAuthApiClient;
    private final FusionAuthProperties fusionAuthProperties;

    /**
     * Login endpoint
     *
     * @param loginRequest request with user and password
     * @return response with token and user data
     */
    @PostMapping("/login")
    public ResponseEntity<FusionAuthLoginResponse>  login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getLoginId());
        
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
            log.error("Error in login for user: {}", loginRequest.getLoginId(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Model for login request
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LoginRequest {
        private String loginId;
        private String password;
    }

}