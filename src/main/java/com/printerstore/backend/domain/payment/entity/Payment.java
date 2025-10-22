package com.printerstore.backend.domain.payment.entity;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    private Long id;
    private String userId;
    private String externalId;
    private BigDecimal amount;
    private String paymentId;
    private String reference;
    private Integer responseCode;
    private String responseMessage;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}