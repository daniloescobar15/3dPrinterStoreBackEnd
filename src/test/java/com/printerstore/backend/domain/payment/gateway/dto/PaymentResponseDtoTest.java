package com.printerstore.backend.domain.payment.gateway.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentResponseDtoTest {

    @Test
    void testPaymentResponseDto_AllArgsConstructor() {
        // Arrange
        PaymentResponseDto.PaymentDataDto data = new PaymentResponseDto.PaymentDataDto(
                12345, "REF-001", 100.0, "Test", "2024-01-01", "PENDING", "Payment created"
        );

        // Act
        PaymentResponseDto dto = new PaymentResponseDto(201, "Payment processed", data);

        // Assert
        assertThat(dto.getResponseCode()).isEqualTo(201);
        assertThat(dto.getResponseMessage()).isEqualTo("Payment processed");
        assertThat(dto.getData()).isNotNull();
        assertThat(dto.getData().getPaymentId()).isEqualTo(12345);
        assertThat(dto.getData().getReference()).isEqualTo("REF-001");
        assertThat(dto.getData().getAmount()).isEqualTo(100.0);
    }

    @Test
    void testPaymentResponseDto_NoArgsConstructor() {
        // Act
        PaymentResponseDto dto = new PaymentResponseDto();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getResponseCode()).isNull();
    }

    @Test
    void testPaymentResponseDto_Setters() {
        // Arrange
        PaymentResponseDto dto = new PaymentResponseDto();
        PaymentResponseDto.PaymentDataDto data = new PaymentResponseDto.PaymentDataDto();

        // Act
        dto.setResponseCode(200);
        dto.setResponseMessage("Success");
        data.setPaymentId(54321);
        data.setReference("REF-002");
        data.setAmount(250.0);
        data.setDescription("Updated");
        data.setCreationDate("2024-01-02");
        data.setStatus("COMPLETED");
        data.setMessage("Complete");
        dto.setData(data);

        // Assert
        assertThat(dto.getResponseCode()).isEqualTo(200);
        assertThat(dto.getResponseMessage()).isEqualTo("Success");
        assertThat(dto.getData().getPaymentId()).isEqualTo(54321);
        assertThat(dto.getData().getReference()).isEqualTo("REF-002");
        assertThat(dto.getData().getAmount()).isEqualTo(250.0);
        assertThat(dto.getData().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void testPaymentDataDto_AllArgsConstructor() {
        // Act
        PaymentResponseDto.PaymentDataDto data = new PaymentResponseDto.PaymentDataDto(
                99999, "REF-999", 999.99, "Large payment", "2024-12-31", "COMPLETED", "Success"
        );

        // Assert
        assertThat(data.getPaymentId()).isEqualTo(99999);
        assertThat(data.getReference()).isEqualTo("REF-999");
        assertThat(data.getAmount()).isEqualTo(999.99);
        assertThat(data.getDescription()).isEqualTo("Large payment");
        assertThat(data.getCreationDate()).isEqualTo("2024-12-31");
        assertThat(data.getStatus()).isEqualTo("COMPLETED");
        assertThat(data.getMessage()).isEqualTo("Success");
    }

    @Test
    void testPaymentDataDto_NoArgsConstructor() {
        // Act
        PaymentResponseDto.PaymentDataDto data = new PaymentResponseDto.PaymentDataDto();

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.getPaymentId()).isNull();
    }
}