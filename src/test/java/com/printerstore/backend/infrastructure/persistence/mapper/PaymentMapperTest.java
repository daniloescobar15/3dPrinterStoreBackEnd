package com.printerstore.backend.infrastructure.persistence.mapper;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.infrastructure.persistence.entity.PaymentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
    }

    @Test
    void testToDomainEntity_MapsAllFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        PaymentEntity entity = PaymentEntity.builder()
                .id(1L)
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(100.00))
                .paymentId("PAY-001")
                .reference("REF-001")
                .responseCode(201)
                .responseMessage("Success")
                .status("COMPLETED")
                .description("Test payment")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Act
        Payment result = paymentMapper.toDomainEntity(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getExternalId()).isEqualTo("EXT-001");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(result.getPaymentId()).isEqualTo("PAY-001");
        assertThat(result.getReference()).isEqualTo("REF-001");
        assertThat(result.getResponseCode()).isEqualTo(201);
        assertThat(result.getResponseMessage()).isEqualTo("Success");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getDescription()).isEqualTo("Test payment");
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testToDomainEntity_WithNullEntity() {
        // Act
        Payment result = paymentMapper.toDomainEntity(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void testToPersistenceEntity_MapsAllFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Payment domain = Payment.builder()
                .id(1L)
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(100.00))
                .paymentId("PAY-001")
                .reference("REF-001")
                .responseCode(201)
                .responseMessage("Success")
                .status("COMPLETED")
                .description("Test payment")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Act
        PaymentEntity result = paymentMapper.toPersistenceEntity(domain);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getExternalId()).isEqualTo("EXT-001");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(result.getPaymentId()).isEqualTo("PAY-001");
        assertThat(result.getReference()).isEqualTo("REF-001");
        assertThat(result.getResponseCode()).isEqualTo(201);
        assertThat(result.getResponseMessage()).isEqualTo("Success");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getDescription()).isEqualTo("Test payment");
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testToPersistenceEntity_WithNullDomain() {
        // Act
        PaymentEntity result = paymentMapper.toPersistenceEntity(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void testBidirectionalMapping() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        PaymentEntity originalEntity = PaymentEntity.builder()
                .id(1L)
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(100.00))
                .paymentId("PAY-001")
                .reference("REF-001")
                .responseCode(201)
                .responseMessage("Success")
                .status("COMPLETED")
                .description("Test payment")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Act
        Payment domain = paymentMapper.toDomainEntity(originalEntity);
        PaymentEntity resultEntity = paymentMapper.toPersistenceEntity(domain);

        // Assert
        assertThat(resultEntity).isNotNull();
        assertThat(resultEntity.getId()).isEqualTo(originalEntity.getId());
        assertThat(resultEntity.getUserId()).isEqualTo(originalEntity.getUserId());
        assertThat(resultEntity.getExternalId()).isEqualTo(originalEntity.getExternalId());
        assertThat(resultEntity.getAmount()).isEqualTo(originalEntity.getAmount());
        assertThat(resultEntity.getPaymentId()).isEqualTo(originalEntity.getPaymentId());
        assertThat(resultEntity.getReference()).isEqualTo(originalEntity.getReference());
        assertThat(resultEntity.getResponseCode()).isEqualTo(originalEntity.getResponseCode());
        assertThat(resultEntity.getResponseMessage()).isEqualTo(originalEntity.getResponseMessage());
        assertThat(resultEntity.getStatus()).isEqualTo(originalEntity.getStatus());
        assertThat(resultEntity.getDescription()).isEqualTo(originalEntity.getDescription());
    }

    @Test
    void testToDomainEntity_WithPartialData() {
        // Arrange
        PaymentEntity entity = PaymentEntity.builder()
                .id(1L)
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(50.00))
                .status("PENDING")
                .build();

        // Act
        Payment result = paymentMapper.toDomainEntity(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getPaymentId()).isNull();
        assertThat(result.getReference()).isNull();
        assertThat(result.getResponseCode()).isNull();
    }
}