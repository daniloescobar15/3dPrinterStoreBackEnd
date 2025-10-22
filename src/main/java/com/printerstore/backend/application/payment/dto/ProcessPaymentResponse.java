package com.printerstore.backend.application.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentResponse {
    private Integer responseCode;
    private String responseMessage;
    private PaymentDataResponse data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDataResponse {
        private Integer paymentId;
        private String reference;
        private Double amount;
        private String description;
        private String creationDate;
        private String status;
        private String message;
    }
}