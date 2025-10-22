package com.printerstore.backend.infrastructure.provider.webclient.puntored.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    
    @JsonProperty("responseCode")
    private Integer responseCode;
    
    @JsonProperty("responseMessage")
    private String responseMessage;
    
    @JsonProperty("data")
    private AuthenticationData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticationData {
        private String token;
        private String createdAt;
    }
}