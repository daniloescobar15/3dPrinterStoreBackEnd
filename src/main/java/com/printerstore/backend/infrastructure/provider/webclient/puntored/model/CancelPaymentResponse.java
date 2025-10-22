package com.printerstore.backend.infrastructure.provider.webclient.puntored.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentResponse {
    
    @JsonProperty("responseCode")
    private Integer responseCode;
    
    @JsonProperty("responseMessage")
    private String responseMessage;
    
    @JsonProperty("data")
    private CancelPaymentData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelPaymentData {
        private Integer paymentId;
        private String creationDate;
        private String reference;
        private String status;
        private String message;
        private String cancelDescription;
        private String updatedAt;
    }
}