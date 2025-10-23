package com.printerstore.backend.domain.payment.gateway.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CancelPaymentResponseDtoTest {

    @Test
    void testCancelPaymentResponseDto_AllArgsConstructor() {
        // Arrange
        CancelPaymentResponseDto.CancelPaymentDataDto data = new CancelPaymentResponseDto.CancelPaymentDataDto(
                12345, "2024-01-01", "REF-001", "03", "Cancelled", "User request", "2024-01-02"
        );

        // Act
        CancelPaymentResponseDto dto = new CancelPaymentResponseDto(202, "Payment cancelled", data);

        // Assert
        assertThat(dto.getResponseCode()).isEqualTo(202);
        assertThat(dto.getResponseMessage()).isEqualTo("Payment cancelled");
        assertThat(dto.getData()).isNotNull();
        assertThat(dto.getData().getPaymentId()).isEqualTo(12345);
        assertThat(dto.getData().getReference()).isEqualTo("REF-001");
        assertThat(dto.getData().getStatus()).isEqualTo("03");
    }

    @Test
    void testCancelPaymentResponseDto_NoArgsConstructor() {
        // Act
        CancelPaymentResponseDto dto = new CancelPaymentResponseDto();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getResponseCode()).isNull();
        assertThat(dto.getData()).isNull();
    }

    @Test
    void testCancelPaymentResponseDto_Setters() {
        // Arrange
        CancelPaymentResponseDto dto = new CancelPaymentResponseDto();
        CancelPaymentResponseDto.CancelPaymentDataDto data = new CancelPaymentResponseDto.CancelPaymentDataDto();

        // Act
        dto.setResponseCode(202);
        dto.setResponseMessage("Accepted");
        data.setPaymentId(54321);
        data.setCreationDate("2024-02-01");
        data.setReference("REF-002");
        data.setStatus("03");
        data.setMessage("Cancelled");
        data.setCancelDescription("Duplicate");
        data.setUpdatedAt("2024-02-02");
        dto.setData(data);

        // Assert
        assertThat(dto.getResponseCode()).isEqualTo(202);
        assertThat(dto.getResponseMessage()).isEqualTo("Accepted");
        assertThat(dto.getData().getPaymentId()).isEqualTo(54321);
        assertThat(dto.getData().getReference()).isEqualTo("REF-002");
        assertThat(dto.getData().getStatus()).isEqualTo("03");
        assertThat(dto.getData().getCancelDescription()).isEqualTo("Duplicate");
    }

    @Test
    void testCancelPaymentDataDto_AllArgsConstructor() {
        // Act
        CancelPaymentResponseDto.CancelPaymentDataDto data = new CancelPaymentResponseDto.CancelPaymentDataDto(
                99999, "2024-12-31", "REF-999", "03", "Cancelled", "Large refund", "2025-01-01"
        );

        // Assert
        assertThat(data.getPaymentId()).isEqualTo(99999);
        assertThat(data.getCreationDate()).isEqualTo("2024-12-31");
        assertThat(data.getReference()).isEqualTo("REF-999");
        assertThat(data.getStatus()).isEqualTo("03");
        assertThat(data.getMessage()).isEqualTo("Cancelled");
        assertThat(data.getCancelDescription()).isEqualTo("Large refund");
        assertThat(data.getUpdatedAt()).isEqualTo("2025-01-01");
    }

    @Test
    void testCancelPaymentDataDto_NoArgsConstructor() {
        // Act
        CancelPaymentResponseDto.CancelPaymentDataDto data = new CancelPaymentResponseDto.CancelPaymentDataDto();

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.getPaymentId()).isNull();
        assertThat(data.getReference()).isNull();
    }
}