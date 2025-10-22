package com.printerstore.backend.application.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentResponse {
    private Integer responseCode;
    private String responseMessage;
    private CancelPaymentDataResponse data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelPaymentDataResponse {
        private Integer paymentId;
        private String creationDate;
        private String reference;
        private String status;
        private String message;
        private String cancelDescription;
        private String updatedAt;
    }
}