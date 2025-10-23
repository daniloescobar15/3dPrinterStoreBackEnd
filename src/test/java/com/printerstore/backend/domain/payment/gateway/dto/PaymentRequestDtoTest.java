package com.printerstore.backend.domain.payment.gateway.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentRequestDtoTest {

    @Test
    void testPaymentRequestDto_AllArgsConstructor() {
        // Arrange
        String externalId = "EXT-001";
        Double amount = 100.00;
        String description = "Test Payment";
        String dueDate = "2024-12-31";
        String callbackURL = "http://callback.test";

        // Act
        PaymentRequestDto dto = new PaymentRequestDto(externalId, amount, description, dueDate, callbackURL);

        // Assert
        assertThat(dto.getExternalId()).isEqualTo(externalId);
        assertThat(dto.getAmount()).isEqualTo(amount);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getDueDate()).isEqualTo(dueDate);
        assertThat(dto.getCallbackURL()).isEqualTo(callbackURL);
    }

    @Test
    void testPaymentRequestDto_NoArgsConstructor() {
        // Act
        PaymentRequestDto dto = new PaymentRequestDto();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getExternalId()).isNull();
        assertThat(dto.getAmount()).isNull();
    }

    @Test
    void testPaymentRequestDto_Setters() {
        // Arrange
        PaymentRequestDto dto = new PaymentRequestDto();

        // Act
        dto.setExternalId("EXT-002");
        dto.setAmount(200.00);
        dto.setDescription("Updated Payment");
        dto.setDueDate("2025-01-15");
        dto.setCallbackURL("http://new-callback.test");

        // Assert
        assertThat(dto.getExternalId()).isEqualTo("EXT-002");
        assertThat(dto.getAmount()).isEqualTo(200.00);
        assertThat(dto.getDescription()).isEqualTo("Updated Payment");
        assertThat(dto.getDueDate()).isEqualTo("2025-01-15");
        assertThat(dto.getCallbackURL()).isEqualTo("http://new-callback.test");
    }
}