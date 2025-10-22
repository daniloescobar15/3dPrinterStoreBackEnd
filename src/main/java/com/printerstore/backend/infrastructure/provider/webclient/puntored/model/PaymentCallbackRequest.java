package com.printerstore.backend.infrastructure.provider.webclient.puntored.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to receive payment update callback from Punto Red
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackRequest {
    
    private Long paymentId;
    private String externalId;
    private Double amount;
    private String authorizationNumber;
    private String reference;
    private Long paymentDate;
    private String status;
    private String message;
}