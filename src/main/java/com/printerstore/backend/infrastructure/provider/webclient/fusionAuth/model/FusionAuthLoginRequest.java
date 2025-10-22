package com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FusionAuthLoginRequest {
    
    @JsonProperty("applicationId")
    private String applicationId;
    
    @JsonProperty("loginId")
    private String loginId;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("metaData")
    private Map<String, Object> metaData;
}