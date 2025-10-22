package com.printerstore.backend.domain.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private Integer responseCode;
    private String responseMessage;
    private PaymentDataDto data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDataDto {
        private Integer paymentId;
        private String reference;
        private Double amount;
        private String description;
        private String creationDate;
        private String status;
        private String message;
    }
}