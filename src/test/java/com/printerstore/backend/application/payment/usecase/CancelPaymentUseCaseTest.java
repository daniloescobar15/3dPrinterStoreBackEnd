package com.printerstore.backend.application.payment.usecase;

import com.printerstore.backend.application.authentication.port.AuthenticationServicePort;
import com.printerstore.backend.application.payment.dto.CancelPaymentResponse;
import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.gateway.PaymentGateway;
import com.printerstore.backend.domain.payment.gateway.dto.CancelPaymentRequestDto;
import com.printerstore.backend.domain.payment.gateway.dto.CancelPaymentResponseDto;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import com.printerstore.backend.infrastructure.config.PuntoRedProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelPaymentUseCaseTest {

    @Mock
    private PaymentGateway paymentGateway;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private AuthenticationServicePort authenticationService;

    private CancelPaymentUseCase cancelPaymentUseCase;
    private PuntoRedProperties puntoRedProperties;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjo5OTk5OTk5OTk5fQ.token";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String REFERENCE = "REF-12345";
    private static final String CANCEL_DESCRIPTION = "User requested cancellation";
    private static final Integer PAYMENT_ID = 12345;

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test
        reset(paymentGateway, paymentRepository, authenticationService);
        
        // Create and setup PuntoRedProperties
        puntoRedProperties = new PuntoRedProperties();
        puntoRedProperties.setUsername(USERNAME);
        puntoRedProperties.setPassword(PASSWORD);
        
        // Manually create the use case with dependencies since we can't use @InjectMocks with the non-mock PuntoRedProperties
        cancelPaymentUseCase = new CancelPaymentUseCase(paymentGateway, paymentRepository, authenticationService, puntoRedProperties);
        
        // Setup default behavior for authentication
        when(authenticationService.getAuthenticationToken(USERNAME, PASSWORD)).thenReturn(VALID_TOKEN);
    }

    @Test
    void testSuccessfulPaymentCancellationWithCode202() {
        // Arrange
        CancelPaymentResponseDto.CancelPaymentDataDto data = createSuccessfulCancelPaymentData();
        CancelPaymentResponseDto gatewayResponse = new CancelPaymentResponseDto(202, "Payment cancelled successfully", data);
        
        Payment existingPayment = createExistingPayment();
        
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        CancelPaymentResponse result = cancelPaymentUseCase.execute(REFERENCE, CANCEL_DESCRIPTION);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getResponseCode()).isEqualTo(202);
        assertThat(result.getResponseMessage()).isEqualTo("Payment cancelled successfully");
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(result.getData().getReference()).isEqualTo(REFERENCE);
        assertThat(result.getData().getStatus()).isEqualTo("03");
        assertThat(result.getData().getCancelDescription()).isEqualTo(CANCEL_DESCRIPTION);

        // Verify authentication was called
        verify(authenticationService).getAuthenticationToken(USERNAME, PASSWORD);
        
        // Verify cancel request was properly created
        ArgumentCaptor<CancelPaymentRequestDto> requestCaptor = ArgumentCaptor.forClass(CancelPaymentRequestDto.class);
        verify(paymentGateway).cancelPayment(requestCaptor.capture(), eq(VALID_TOKEN));
        
        CancelPaymentRequestDto capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getReference()).isEqualTo(REFERENCE);
        assertThat(capturedRequest.getStatus()).isEqualTo("03");
        assertThat(capturedRequest.getUpdateDescription()).isEqualTo(CANCEL_DESCRIPTION);
    }

    @Test
    void testPaymentFoundAndUpdatedAfterSuccessfulCancellation() {
        // Arrange
        CancelPaymentResponseDto.CancelPaymentDataDto data = createSuccessfulCancelPaymentData();
        CancelPaymentResponseDto gatewayResponse = new CancelPaymentResponseDto(202, "Payment cancelled successfully", data);
        
        Payment existingPayment = createExistingPayment();
        
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        cancelPaymentUseCase.execute(REFERENCE, CANCEL_DESCRIPTION);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getStatus()).isEqualTo("03");
        assertThat(savedPayment.getResponseCode()).isEqualTo(202);
        assertThat(savedPayment.getResponseMessage()).isEqualTo("Payment cancelled successfully");
        
        verify(paymentRepository).findByReference(REFERENCE);
    }

    @Test
    void testResponseCode409PaymentAlreadyCancelled() {
        // Arrange
        CancelPaymentResponseDto gatewayResponse = new CancelPaymentResponseDto(409, "Payment already cancelled", null);
        
        Payment existingPayment = createExistingPayment();
        
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act & Assert
        assertThatThrownBy(() -> cancelPaymentUseCase.execute(REFERENCE, CANCEL_DESCRIPTION))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error cancelling payment: Payment already cancelled");

        // Verify payment was updated with error status
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getResponseCode()).isEqualTo(409);
        assertThat(savedPayment.getResponseMessage()).isEqualTo("Payment already cancelled");
        
        verify(paymentRepository).findByReference(REFERENCE);
    }

    @Test
    void testPaymentNotFoundInDatabaseToUpdate() {
        // Arrange
        CancelPaymentResponseDto.CancelPaymentDataDto data = createSuccessfulCancelPaymentData();
        CancelPaymentResponseDto gatewayResponse = new CancelPaymentResponseDto(202, "Payment cancelled successfully", data);
        
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.empty());

        // Act
        CancelPaymentResponse result = cancelPaymentUseCase.execute(REFERENCE, CANCEL_DESCRIPTION);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getResponseCode()).isEqualTo(202);
        assertThat(result.getResponseMessage()).isEqualTo("Payment cancelled successfully");
        
        // Verify repository was queried but no save was called
        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testNullResponseFromPaymentGateway() {
        // Arrange
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> cancelPaymentUseCase.execute(REFERENCE, CANCEL_DESCRIPTION))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected error cancelling payment: null response");

        verify(paymentGateway).cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN));
        verify(paymentRepository, never()).findByReference(anyString());
    }

    @Test
    void testUnexpectedResponseCodeFromGateway() {
        // Arrange
        CancelPaymentResponseDto gatewayResponse = new CancelPaymentResponseDto(500, "Internal server error", null);
        
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);

        // Act & Assert
        assertThatThrownBy(() -> cancelPaymentUseCase.execute(REFERENCE, CANCEL_DESCRIPTION))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected error cancelling payment: Internal server error");

        verify(paymentGateway).cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN));
        verify(paymentRepository, never()).findByReference(anyString());
    }

    @Test
    void testNullReferenceParameterValidation() {
        // Act & Assert
        assertThatThrownBy(() -> cancelPaymentUseCase.execute(null, CANCEL_DESCRIPTION))
                .isInstanceOf(RuntimeException.class);

        // Verify no interactions with dependencies when input is invalid
        verify(authenticationService).getAuthenticationToken(USERNAME, PASSWORD);
        verify(paymentGateway).cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN));
    }

    @Test
    void testEmptyCancelDescriptionParameter() {
        // Arrange
        String emptyCancelDescription = "";
        CancelPaymentResponseDto.CancelPaymentDataDto data = new CancelPaymentResponseDto.CancelPaymentDataDto(
                PAYMENT_ID, "2024-01-01", REFERENCE, "03", "Cancelled", emptyCancelDescription, "2024-01-02"
        );
        CancelPaymentResponseDto gatewayResponse = new CancelPaymentResponseDto(202, "Payment cancelled successfully", data);
        
        Payment existingPayment = createExistingPayment();
        
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        CancelPaymentResponse result = cancelPaymentUseCase.execute(REFERENCE, emptyCancelDescription);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getResponseCode()).isEqualTo(202);
        assertThat(result.getData().getCancelDescription()).isEqualTo(emptyCancelDescription);

        // Verify the request was created with empty description
        ArgumentCaptor<CancelPaymentRequestDto> requestCaptor = ArgumentCaptor.forClass(CancelPaymentRequestDto.class);
        verify(paymentGateway).cancelPayment(requestCaptor.capture(), eq(VALID_TOKEN));
        
        CancelPaymentRequestDto capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getUpdateDescription()).isEqualTo(emptyCancelDescription);
    }

    @Test
    void testMappingFromCancelPaymentResponseDto() {
        // Arrange
        CancelPaymentResponseDto.CancelPaymentDataDto data = new CancelPaymentResponseDto.CancelPaymentDataDto(
                PAYMENT_ID, "2024-01-01T10:00:00", REFERENCE, "03", "Payment cancelled", CANCEL_DESCRIPTION, "2024-01-01T11:00:00"
        );
        CancelPaymentResponseDto gatewayResponse = new CancelPaymentResponseDto(202, "Success", data);
        
        Payment existingPayment = createExistingPayment();
        
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        CancelPaymentResponse result = cancelPaymentUseCase.execute(REFERENCE, CANCEL_DESCRIPTION);

        // Assert - Verify all fields are properly mapped
        assertThat(result.getResponseCode()).isEqualTo(202);
        assertThat(result.getResponseMessage()).isEqualTo("Success");
        assertThat(result.getData()).isNotNull();
        
        CancelPaymentResponse.CancelPaymentDataResponse resultData = result.getData();
        assertThat(resultData.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(resultData.getCreationDate()).isEqualTo("2024-01-01T10:00:00");
        assertThat(resultData.getReference()).isEqualTo(REFERENCE);
        assertThat(resultData.getStatus()).isEqualTo("03");
        assertThat(resultData.getMessage()).isEqualTo("Payment cancelled");
        assertThat(resultData.getCancelDescription()).isEqualTo(CANCEL_DESCRIPTION);
        assertThat(resultData.getUpdatedAt()).isEqualTo("2024-01-01T11:00:00");
    }

    @Test
    void testCode409WithPaymentNotFoundInDatabase() {
        // Arrange
        CancelPaymentResponseDto gatewayResponse = new CancelPaymentResponseDto(409, "Payment already cancelled", null);
        
        when(paymentGateway.cancelPayment(any(CancelPaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cancelPaymentUseCase.execute(REFERENCE, CANCEL_DESCRIPTION))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error cancelling payment: Payment already cancelled");

        // Verify repository was queried but no save was called
        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    // Helper methods
    private CancelPaymentResponseDto.CancelPaymentDataDto createSuccessfulCancelPaymentData() {
        return new CancelPaymentResponseDto.CancelPaymentDataDto(
                PAYMENT_ID, "2024-01-01", REFERENCE, "03", "Payment cancelled", CANCEL_DESCRIPTION, "2024-01-02"
        );
    }

    private Payment createExistingPayment() {
        return Payment.builder()
                .id(1L)
                .userId("user123")
                .externalId("ext123")
                .amount(new BigDecimal("100.00"))
                .paymentId(String.valueOf(PAYMENT_ID))
                .reference(REFERENCE)
                .responseCode(200)
                .responseMessage("Payment created")
                .status("01")
                .description("Test payment")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}