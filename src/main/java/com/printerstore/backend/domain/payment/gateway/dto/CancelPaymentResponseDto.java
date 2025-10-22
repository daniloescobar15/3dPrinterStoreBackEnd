package com.printerstore.backend.domain.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentResponseDto {
    private Integer responseCode;
    private String responseMessage;
    private CancelPaymentDataDto data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelPaymentDataDto {
        private Integer paymentId;
        private String creationDate;
        private String reference;
        private String status;
        private String message;
        private String cancelDescription;
        private String updatedAt;
    }
}