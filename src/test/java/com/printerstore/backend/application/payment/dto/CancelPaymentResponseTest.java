package com.printerstore.backend.application.payment.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CancelPaymentResponseTest {

    @Test
    void testCancelPaymentResponse_Creation() {
        // Act
        CancelPaymentResponse response = new CancelPaymentResponse();

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    void testCancelPaymentResponse_Setters() {
        // Arrange
        CancelPaymentResponse response = new CancelPaymentResponse();
        CancelPaymentResponse.CancelPaymentDataResponse dataResponse = new CancelPaymentResponse.CancelPaymentDataResponse();

        dataResponse.setPaymentId(12345);
        dataResponse.setReference("REF-001");
        dataResponse.setCreationDate("2024-01-01");
        dataResponse.setStatus("03");
        dataResponse.setMessage("Payment cancelled");
        dataResponse.setCancelDescription("User requested cancellation");
        dataResponse.setUpdatedAt("2024-01-02");

        response.setResponseCode(202);
        response.setResponseMessage("Payment cancelled successfully");
        response.setData(dataResponse);

        // Assert
        assertThat(response.getResponseCode()).isEqualTo(202);
        assertThat(response.getResponseMessage()).isEqualTo("Payment cancelled successfully");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getPaymentId()).isEqualTo(12345);
        assertThat(response.getData().getReference()).isEqualTo("REF-001");
        assertThat(response.getData().getStatus()).isEqualTo("03");
        assertThat(response.getData().getCancelDescription()).isEqualTo("User requested cancellation");
    }

    @Test
    void testCancelPaymentDataResponse_AllFields() {
        // Arrange
        CancelPaymentResponse.CancelPaymentDataResponse data = new CancelPaymentResponse.CancelPaymentDataResponse();

        // Act
        data.setPaymentId(54321);
        data.setReference("REF-002");
        data.setCreationDate("2024-01-15");
        data.setStatus("03");
        data.setMessage("Cancelled");
        data.setCancelDescription("Duplicate charge");
        data.setUpdatedAt("2024-01-16");

        // Assert
        assertThat(data.getPaymentId()).isEqualTo(54321);
        assertThat(data.getReference()).isEqualTo("REF-002");
        assertThat(data.getCreationDate()).isEqualTo("2024-01-15");
        assertThat(data.getStatus()).isEqualTo("03");
        assertThat(data.getMessage()).isEqualTo("Cancelled");
        assertThat(data.getCancelDescription()).isEqualTo("Duplicate charge");
        assertThat(data.getUpdatedAt()).isEqualTo("2024-01-16");
    }

    @Test
    void testCancelPaymentResponse_ErrorResponse() {
        // Arrange
        CancelPaymentResponse response = new CancelPaymentResponse();

        // Act
        response.setResponseCode(409);
        response.setResponseMessage("Cannot cancel this payment");
        response.setData(null);

        // Assert
        assertThat(response.getResponseCode()).isEqualTo(409);
        assertThat(response.getResponseMessage()).isEqualTo("Cannot cancel this payment");
        assertThat(response.getData()).isNull();
    }
}