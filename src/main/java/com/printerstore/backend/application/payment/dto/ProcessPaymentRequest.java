package com.printerstore.backend.application.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    private String externalId;
    private Double amount;
    private String description;
    private String dueDate;
    private String callbackURL;
}