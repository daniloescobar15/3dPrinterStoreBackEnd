package com.printerstore.backend.application.payment.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProcessPaymentRequestTest {

    @Test
    void testProcessPaymentRequest_Creation() {
        // Arrange
        String externalId = "EXT-001";
        Double amount = 100.00;
        String description = "Test Payment";
        String dueDate = "2024-12-31";
        String callbackURL = "http://callback.test";

        // Act
        ProcessPaymentRequest request = new ProcessPaymentRequest(externalId, amount, description, dueDate, callbackURL);

        // Assert
        assertThat(request.getExternalId()).isEqualTo(externalId);
        assertThat(request.getAmount()).isEqualTo(amount);
        assertThat(request.getDescription()).isEqualTo(description);
        assertThat(request.getDueDate()).isEqualTo(dueDate);
        assertThat(request.getCallbackURL()).isEqualTo(callbackURL);
    }

    @Test
    void testProcessPaymentRequest_NoArgsConstructor() {
        // Act
        ProcessPaymentRequest request = new ProcessPaymentRequest();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getExternalId()).isNull();
        assertThat(request.getAmount()).isNull();
    }

    @Test
    void testProcessPaymentRequest_Setters() {
        // Arrange
        ProcessPaymentRequest request = new ProcessPaymentRequest();

        // Act
        request.setExternalId("EXT-002");
        request.setAmount(200.00);
        request.setDescription("Updated Payment");
        request.setDueDate("2024-12-31");
        request.setCallbackURL("http://new-callback.test");

        // Assert
        assertThat(request.getExternalId()).isEqualTo("EXT-002");
        assertThat(request.getAmount()).isEqualTo(200.00);
        assertThat(request.getDescription()).isEqualTo("Updated Payment");
        assertThat(request.getDueDate()).isEqualTo("2024-12-31");
        assertThat(request.getCallbackURL()).isEqualTo("http://new-callback.test");
    }

    @Test
    void testProcessPaymentRequest_Equality() {
        // Arrange
        ProcessPaymentRequest request1 = new ProcessPaymentRequest("EXT-001", 100.00, "Test", "2024-12-31", "http://callback.test");
        ProcessPaymentRequest request2 = new ProcessPaymentRequest("EXT-001", 100.00, "Test", "2024-12-31", "http://callback.test");
        ProcessPaymentRequest request3 = new ProcessPaymentRequest("EXT-002", 100.00, "Test", "2024-12-31", "http://callback.test");

        // Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
    }

    @Test
    void testProcessPaymentRequest_HashCode() {
        // Arrange
        ProcessPaymentRequest request1 = new ProcessPaymentRequest("EXT-001", 100.00, "Test", "2024-12-31", "http://callback.test");
        ProcessPaymentRequest request2 = new ProcessPaymentRequest("EXT-001", 100.00, "Test", "2024-12-31", "http://callback.test");

        // Assert
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }
}