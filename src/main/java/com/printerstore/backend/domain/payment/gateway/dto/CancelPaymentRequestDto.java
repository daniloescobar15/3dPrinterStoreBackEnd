package com.printerstore.backend.domain.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentRequestDto {
    private String reference;
    private String status;
    private String updateDescription;
}