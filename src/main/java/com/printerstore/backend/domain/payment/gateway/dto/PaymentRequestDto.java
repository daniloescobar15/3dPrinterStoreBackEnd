package com.printerstore.backend.domain.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private String externalId;
    private Double amount;
    private String description;
    private String dueDate;
    private String callbackURL;
}