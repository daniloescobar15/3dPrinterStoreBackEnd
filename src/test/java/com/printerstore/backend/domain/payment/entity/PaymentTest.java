package com.printerstore.backend.domain.payment.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    void testPayment_BuilderCreation() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        String userId = "user-123";
        String externalId = "EXT-001";
        BigDecimal amount = BigDecimal.valueOf(100.00);

        // Act
        Payment payment = Payment.builder()
                .id(1L)
                .userId(userId)
                .externalId(externalId)
                .amount(amount)
                .paymentId("PAY-001")
                .reference("REF-001")
                .responseCode(201)
                .responseMessage("Success")
                .status("COMPLETED")
                .description("Test payment")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Assert
        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getUserId()).isEqualTo(userId);
        assertThat(payment.getExternalId()).isEqualTo(externalId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getPaymentId()).isEqualTo("PAY-001");
        assertThat(payment.getReference()).isEqualTo("REF-001");
        assertThat(payment.getResponseCode()).isEqualTo(201);
        assertThat(payment.getResponseMessage()).isEqualTo("Success");
        assertThat(payment.getStatus()).isEqualTo("COMPLETED");
        assertThat(payment.getDescription()).isEqualTo("Test payment");
    }

    @Test
    void testPayment_NoArgsConstructor() {
        // Act
        Payment payment = new Payment();

        // Assert
        assertThat(payment).isNotNull();
        assertThat(payment.getId()).isNull();
        assertThat(payment.getUserId()).isNull();
    }

    @Test
    void testPayment_AllArgsConstructor() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        Payment payment = new Payment(
                1L,
                "user-123",
                "EXT-001",
                BigDecimal.valueOf(100.00),
                "PAY-001",
                "REF-001",
                201,
                "Success",
                "COMPLETED",
                "Test payment",
                now,
                now
        );

        // Assert
        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getUserId()).isEqualTo("user-123");
        assertThat(payment.getExternalId()).isEqualTo("EXT-001");
        assertThat(payment.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void testPayment_Setters() {
        // Arrange
        Payment payment = new Payment();

        // Act
        payment.setId(2L);
        payment.setUserId("user-456");
        payment.setExternalId("EXT-002");
        payment.setAmount(BigDecimal.valueOf(250.50));
        payment.setStatus("PENDING");
        payment.setDescription("Updated payment");

        // Assert
        assertThat(payment.getId()).isEqualTo(2L);
        assertThat(payment.getUserId()).isEqualTo("user-456");
        assertThat(payment.getExternalId()).isEqualTo("EXT-002");
        assertThat(payment.getAmount()).isEqualTo(BigDecimal.valueOf(250.50));
        assertThat(payment.getStatus()).isEqualTo("PENDING");
        assertThat(payment.getDescription()).isEqualTo("Updated payment");
    }

    @Test
    void testPayment_Equality() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Payment payment1 = Payment.builder()
                .id(1L)
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(100.00))
                .createdAt(now)
                .build();

        Payment payment2 = Payment.builder()
                .id(1L)
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(100.00))
                .createdAt(now)
                .build();

        Payment payment3 = Payment.builder()
                .id(2L)
                .userId("user-456")
                .externalId("EXT-002")
                .amount(BigDecimal.valueOf(200.00))
                .createdAt(now)
                .build();

        // Assert
        assertThat(payment1).isEqualTo(payment2);
        assertThat(payment1).isNotEqualTo(payment3);
    }

    @Test
    void testPayment_HashCode() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Payment payment1 = Payment.builder()
                .id(1L)
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(100.00))
                .createdAt(now)
                .build();

        Payment payment2 = Payment.builder()
                .id(1L)
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(100.00))
                .createdAt(now)
                .build();

        // Assert
        assertThat(payment1.hashCode()).isEqualTo(payment2.hashCode());
    }

    @Test
    void testPayment_MinimalFields() {
        // Act
        Payment payment = Payment.builder()
                .userId("user-123")
                .externalId("EXT-001")
                .amount(BigDecimal.valueOf(100.00))
                .status("PENDING")
                .build();

        // Assert
        assertThat(payment.getUserId()).isEqualTo("user-123");
        assertThat(payment.getExternalId()).isEqualTo("EXT-001");
        assertThat(payment.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(payment.getStatus()).isEqualTo("PENDING");
        assertThat(payment.getId()).isNull();
        assertThat(payment.getPaymentId()).isNull();
    }
}