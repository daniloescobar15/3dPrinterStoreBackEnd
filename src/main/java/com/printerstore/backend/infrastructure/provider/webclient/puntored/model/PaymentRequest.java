package com.printerstore.backend.infrastructure.provider.webclient.puntored.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    @JsonProperty("externalId")
    private String externalId;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("dueDate")
    private String dueDate;
    
    @JsonProperty("callbackURL")
    private String callbackURL;
}