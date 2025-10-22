package com.printerstore.backend.infrastructure.provider.webclient.puntored.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    @JsonProperty("responseCode")
    private Integer responseCode;
    
    @JsonProperty("responseMessage")
    private String responseMessage;
    
    @JsonProperty("data")
    private PaymentData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentData {
        private Integer paymentId;
        private String reference;
        private Double amount;
        private String description;
        private String creationDate;
        private String status;
        private String message;
    }
}