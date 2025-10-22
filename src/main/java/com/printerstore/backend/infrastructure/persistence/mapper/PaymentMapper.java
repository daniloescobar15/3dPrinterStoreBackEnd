package com.printerstore.backend.infrastructure.persistence.mapper;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toDomainEntity(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }
        return Payment.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .externalId(entity.getExternalId())
                .amount(entity.getAmount())
                .paymentId(entity.getPaymentId())
                .reference(entity.getReference())
                .responseCode(entity.getResponseCode())
                .responseMessage(entity.getResponseMessage())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PaymentEntity toPersistenceEntity(Payment domain) {
        if (domain == null) {
            return null;
        }
        return PaymentEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .externalId(domain.getExternalId())
                .amount(domain.getAmount())
                .paymentId(domain.getPaymentId())
                .reference(domain.getReference())
                .responseCode(domain.getResponseCode())
                .responseMessage(domain.getResponseMessage())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}