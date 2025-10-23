package com.printerstore.backend.infrastructure.adapter.in.web.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void testErrorResponse_BuilderCreation() {
        // Arrange
        Integer responseCode = 400;
        String responseMessage = "Bad Request";
        String error = "Invalid input";
        Long timestamp = System.currentTimeMillis();

        // Act
        ErrorResponse response = ErrorResponse.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .error(error)
                .timestamp(timestamp)
                .build();

        // Assert
        assertThat(response.getResponseCode()).isEqualTo(responseCode);
        assertThat(response.getResponseMessage()).isEqualTo(responseMessage);
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void testErrorResponse_NoArgsConstructor() {
        // Act
        ErrorResponse response = new ErrorResponse();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getResponseCode()).isNull();
        assertThat(response.getResponseMessage()).isNull();
    }

    @Test
    void testErrorResponse_AllArgsConstructor() {
        // Arrange
        Integer responseCode = 401;
        String responseMessage = "Unauthorized";
        String error = "Invalid token";
        Long timestamp = 123456789L;

        // Act
        ErrorResponse response = new ErrorResponse(responseCode, responseMessage, error, timestamp);

        // Assert
        assertThat(response.getResponseCode()).isEqualTo(responseCode);
        assertThat(response.getResponseMessage()).isEqualTo(responseMessage);
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void testErrorResponse_Setters() {
        // Arrange
        ErrorResponse response = new ErrorResponse();

        // Act
        response.setResponseCode(500);
        response.setResponseMessage("Internal Server Error");
        response.setError("Database connection failed");
        response.setTimestamp(System.currentTimeMillis());

        // Assert
        assertThat(response.getResponseCode()).isEqualTo(500);
        assertThat(response.getResponseMessage()).isEqualTo("Internal Server Error");
        assertThat(response.getError()).isEqualTo("Database connection failed");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testErrorResponse_Equality() {
        // Arrange
        Long timestamp = 123456789L;
        ErrorResponse response1 = ErrorResponse.builder()
                .responseCode(400)
                .responseMessage("Bad Request")
                .error("Invalid input")
                .timestamp(timestamp)
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .responseCode(400)
                .responseMessage("Bad Request")
                .error("Invalid input")
                .timestamp(timestamp)
                .build();

        ErrorResponse response3 = ErrorResponse.builder()
                .responseCode(500)
                .responseMessage("Internal Server Error")
                .error("Server error")
                .timestamp(timestamp)
                .build();

        // Assert
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
    }

    @Test
    void testErrorResponse_HashCode() {
        // Arrange
        Long timestamp = 123456789L;
        ErrorResponse response1 = ErrorResponse.builder()
                .responseCode(400)
                .responseMessage("Bad Request")
                .error("Invalid input")
                .timestamp(timestamp)
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .responseCode(400)
                .responseMessage("Bad Request")
                .error("Invalid input")
                .timestamp(timestamp)
                .build();

        // Assert
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void testErrorResponse_ToStringContainsFields() {
        // Arrange
        ErrorResponse response = ErrorResponse.builder()
                .responseCode(400)
                .responseMessage("Bad Request")
                .error("Invalid input")
                .timestamp(123456789L)
                .build();

        // Act
        String toString = response.toString();

        // Assert
        assertThat(toString).contains("responseCode");
        assertThat(toString).contains("400");
    }
}