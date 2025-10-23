package com.printerstore.backend.application.payment.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProcessPaymentResponseTest {

    @Test
    void testProcessPaymentResponse_Creation() {
        // Act
        ProcessPaymentResponse response = new ProcessPaymentResponse();

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    void testProcessPaymentResponse_Setters() {
        // Arrange
        ProcessPaymentResponse response = new ProcessPaymentResponse();
        ProcessPaymentResponse.PaymentDataResponse dataResponse = new ProcessPaymentResponse.PaymentDataResponse();

        dataResponse.setPaymentId(12345);
        dataResponse.setReference("REF-001");
        dataResponse.setAmount(100.00);
        dataResponse.setDescription("Test Payment");
        dataResponse.setCreationDate("2024-01-01");
        dataResponse.setStatus("PENDING");
        dataResponse.setMessage("Payment created");

        response.setResponseCode(201);
        response.setResponseMessage("Payment processed successfully");
        response.setData(dataResponse);

        // Assert
        assertThat(response.getResponseCode()).isEqualTo(201);
        assertThat(response.getResponseMessage()).isEqualTo("Payment processed successfully");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getPaymentId()).isEqualTo(12345);
        assertThat(response.getData().getReference()).isEqualTo("REF-001");
        assertThat(response.getData().getAmount()).isEqualTo(100.00);
        assertThat(response.getData().getDescription()).isEqualTo("Test Payment");
        assertThat(response.getData().getStatus()).isEqualTo("PENDING");
    }

    @Test
    void testPaymentDataResponse_AllFields() {
        // Arrange
        ProcessPaymentResponse.PaymentDataResponse data = new ProcessPaymentResponse.PaymentDataResponse();

        // Act
        data.setPaymentId(54321);
        data.setReference("REF-002");
        data.setAmount(250.50);
        data.setDescription("Premium Payment");
        data.setCreationDate("2024-01-15");
        data.setStatus("COMPLETED");
        data.setMessage("Payment completed successfully");

        // Assert
        assertThat(data.getPaymentId()).isEqualTo(54321);
        assertThat(data.getReference()).isEqualTo("REF-002");
        assertThat(data.getAmount()).isEqualTo(250.50);
        assertThat(data.getDescription()).isEqualTo("Premium Payment");
        assertThat(data.getCreationDate()).isEqualTo("2024-01-15");
        assertThat(data.getStatus()).isEqualTo("COMPLETED");
        assertThat(data.getMessage()).isEqualTo("Payment completed successfully");
    }

    @Test
    void testProcessPaymentResponse_NullData() {
        // Arrange
        ProcessPaymentResponse response = new ProcessPaymentResponse();

        // Act
        response.setResponseCode(400);
        response.setResponseMessage("Bad Request");
        response.setData(null);

        // Assert
        assertThat(response.getResponseCode()).isEqualTo(400);
        assertThat(response.getResponseMessage()).isEqualTo("Bad Request");
        assertThat(response.getData()).isNull();
    }
}