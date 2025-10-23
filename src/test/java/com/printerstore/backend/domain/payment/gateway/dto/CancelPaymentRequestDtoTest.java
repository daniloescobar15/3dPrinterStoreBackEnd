package com.printerstore.backend.domain.payment.gateway.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CancelPaymentRequestDtoTest {

    @Test
    void testCancelPaymentRequestDto_Creation() {
        // Act
        CancelPaymentRequestDto dto = new CancelPaymentRequestDto();

        // Assert
        assertThat(dto).isNotNull();
    }

    @Test
    void testCancelPaymentRequestDto_Setters() {
        // Arrange
        CancelPaymentRequestDto dto = new CancelPaymentRequestDto();

        // Act
        dto.setReference("REF-001");
        dto.setStatus("03");
        dto.setUpdateDescription("User requested cancellation");

        // Assert
        assertThat(dto.getReference()).isEqualTo("REF-001");
        assertThat(dto.getStatus()).isEqualTo("03");
        assertThat(dto.getUpdateDescription()).isEqualTo("User requested cancellation");
    }

    @Test
    void testCancelPaymentRequestDto_MultipleOperations() {
        // Arrange
        CancelPaymentRequestDto dto = new CancelPaymentRequestDto();

        // Act
        dto.setReference("REF-002");
        dto.setStatus("03");
        dto.setUpdateDescription("Refund requested");

        String reference = dto.getReference();
        String status = dto.getStatus();
        String description = dto.getUpdateDescription();

        // Assert
        assertThat(reference).isEqualTo("REF-002");
        assertThat(status).isEqualTo("03");
        assertThat(description).isEqualTo("Refund requested");
    }
}