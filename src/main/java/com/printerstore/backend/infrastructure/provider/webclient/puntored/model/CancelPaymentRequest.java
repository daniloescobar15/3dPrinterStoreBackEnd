package com.printerstore.backend.infrastructure.provider.webclient.puntored.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentRequest {
    
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("updateDescription")
    private String updateDescription;
}