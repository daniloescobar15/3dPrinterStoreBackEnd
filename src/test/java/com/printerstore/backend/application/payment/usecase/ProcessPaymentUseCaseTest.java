package com.printerstore.backend.application.payment.usecase;

import com.printerstore.backend.application.authentication.port.AuthenticationServicePort;
import com.printerstore.backend.application.payment.dto.ProcessPaymentRequest;
import com.printerstore.backend.application.payment.dto.ProcessPaymentResponse;
import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.gateway.PaymentGateway;
import com.printerstore.backend.domain.payment.gateway.dto.PaymentRequestDto;
import com.printerstore.backend.domain.payment.gateway.dto.PaymentResponseDto;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import com.printerstore.backend.infrastructure.config.PuntoRedProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentUseCaseTest {

    @Mock
    private PaymentGateway paymentGateway;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private AuthenticationServicePort authenticationService;

    private ProcessPaymentUseCase processPaymentUseCase;
    private PuntoRedProperties puntoRedProperties;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjo5OTk5OTk5OTk5fQ.token";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String USER_ID = "user123";
    private static final String EXTERNAL_ID = "EXT-12345";
    private static final Double AMOUNT = 100.00;
    private static final String DESCRIPTION = "Test payment description";
    private static final String DUE_DATE = "2024-12-31";
    private static final String CALLBACK_URL = "http://callback.test";
    private static final Integer PAYMENT_ID = 98765;
    private static final String REFERENCE = "REF-98765";

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test
        reset(paymentGateway, paymentRepository, authenticationService);
        
        // Create and setup PuntoRedProperties
        puntoRedProperties = new PuntoRedProperties();
        puntoRedProperties.setUsername(USERNAME);
        puntoRedProperties.setPassword(PASSWORD);
        
        // Manually create the use case with dependencies
        processPaymentUseCase = new ProcessPaymentUseCase(paymentGateway, paymentRepository, authenticationService, puntoRedProperties);
        
        // Setup default behavior for authentication
        when(authenticationService.getAuthenticationToken(USERNAME, PASSWORD)).thenReturn(VALID_TOKEN);
    }

    @Test
    void testSuccessfulPaymentProcessing() {
        // Arrange
        ProcessPaymentRequest request = createProcessPaymentRequest();
        PaymentResponseDto.PaymentDataDto data = createSuccessfulPaymentData();
        PaymentResponseDto gatewayResponse = new PaymentResponseDto(201, "Payment processed successfully", data);
        
        when(paymentGateway.processPayment(any(PaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        // Act
        ProcessPaymentResponse result = processPaymentUseCase.execute(request, USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getResponseCode()).isEqualTo(201);
        assertThat(result.getResponseMessage()).isEqualTo("Payment processed successfully");
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(result.getData().getReference()).isEqualTo(REFERENCE);
        assertThat(result.getData().getAmount()).isEqualTo(AMOUNT);
        assertThat(result.getData().getDescription()).isEqualTo(DESCRIPTION);
        assertThat(result.getData().getStatus()).isEqualTo("PENDING");

        // Verify authentication was called
        verify(authenticationService).getAuthenticationToken(USERNAME, PASSWORD);
        
        // Verify payment gateway was called with correct parameters
        ArgumentCaptor<PaymentRequestDto> requestCaptor = ArgumentCaptor.forClass(PaymentRequestDto.class);
        verify(paymentGateway).processPayment(requestCaptor.capture(), eq(VALID_TOKEN));
        
        PaymentRequestDto capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(capturedRequest.getAmount()).isEqualTo(AMOUNT);
        assertThat(capturedRequest.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(capturedRequest.getDueDate()).isEqualTo(DUE_DATE);
        assertThat(capturedRequest.getCallbackURL()).isEqualTo(CALLBACK_URL);
    }

    @Test
    void testPaymentFailureHandling() {
        // Arrange
        ProcessPaymentRequest request = createProcessPaymentRequest();
        PaymentResponseDto gatewayResponse = new PaymentResponseDto(400, "Invalid payment data", null);
        
        when(paymentGateway.processPayment(any(PaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        // Act & Assert
        assertThatThrownBy(() -> processPaymentUseCase.execute(request, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing payment: Invalid payment data");

        // Verify that failed payment was saved
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getUserId()).isEqualTo(USER_ID);
        assertThat(savedPayment.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(AMOUNT));
        assertThat(savedPayment.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(savedPayment.getResponseCode()).isEqualTo(400);
        assertThat(savedPayment.getResponseMessage()).isEqualTo("Invalid payment data");
        assertThat(savedPayment.getStatus()).isEqualTo("FAILED");
        assertThat(savedPayment.getPaymentId()).isNull();
        assertThat(savedPayment.getReference()).isNull();
    }

    @Test
    void testNullResponseHandling() {
        // Arrange
        ProcessPaymentRequest request = createProcessPaymentRequest();
        
        when(paymentGateway.processPayment(any(PaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        // Act & Assert
        assertThatThrownBy(() -> processPaymentUseCase.execute(request, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing payment: null response");

        // Verify that failed payment was saved with null response handling
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getUserId()).isEqualTo(USER_ID);
        assertThat(savedPayment.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(AMOUNT));
        assertThat(savedPayment.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(savedPayment.getResponseCode()).isNull();
        assertThat(savedPayment.getResponseMessage()).isEqualTo("Null response");
        assertThat(savedPayment.getStatus()).isEqualTo("FAILED");
    }

    @Test
    void testInvalidResponseCode() {
        // Arrange
        ProcessPaymentRequest request = createProcessPaymentRequest();
        PaymentResponseDto.PaymentDataDto data = createSuccessfulPaymentData();
        PaymentResponseDto gatewayResponse = new PaymentResponseDto(500, "Internal server error", data);
        
        when(paymentGateway.processPayment(any(PaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        // Act & Assert
        assertThatThrownBy(() -> processPaymentUseCase.execute(request, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing payment: Internal server error");

        // Verify that failed payment was saved with error details
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getResponseCode()).isEqualTo(500);
        assertThat(savedPayment.getResponseMessage()).isEqualTo("Internal server error");
        assertThat(savedPayment.getStatus()).isEqualTo("PENDING"); // Status from data when available
    }

    @Test
    void testResponseMappingVerification() {
        // Arrange
        ProcessPaymentRequest request = createProcessPaymentRequest();
        PaymentResponseDto.PaymentDataDto data = new PaymentResponseDto.PaymentDataDto(
                PAYMENT_ID, REFERENCE, AMOUNT, DESCRIPTION, "2024-01-01T10:00:00", "COMPLETED", "Payment successful"
        );
        PaymentResponseDto gatewayResponse = new PaymentResponseDto(201, "Success", data);
        
        when(paymentGateway.processPayment(any(PaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        // Act
        ProcessPaymentResponse result = processPaymentUseCase.execute(request, USER_ID);

        // Assert - Verify all fields are properly mapped
        assertThat(result.getResponseCode()).isEqualTo(201);
        assertThat(result.getResponseMessage()).isEqualTo("Success");
        assertThat(result.getData()).isNotNull();
        
        ProcessPaymentResponse.PaymentDataResponse resultData = result.getData();
        assertThat(resultData.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(resultData.getReference()).isEqualTo(REFERENCE);
        assertThat(resultData.getAmount()).isEqualTo(AMOUNT);
        assertThat(resultData.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(resultData.getCreationDate()).isEqualTo("2024-01-01T10:00:00");
        assertThat(resultData.getStatus()).isEqualTo("COMPLETED");
        assertThat(resultData.getMessage()).isEqualTo("Payment successful");
    }

    @Test
    void testAuthenticationTokenRetrieval() {
        // Arrange
        reset(authenticationService); // Clear the default stubbing
        
        ProcessPaymentRequest request = createProcessPaymentRequest();
        PaymentResponseDto.PaymentDataDto data = createSuccessfulPaymentData();
        PaymentResponseDto gatewayResponse = new PaymentResponseDto(201, "Payment processed successfully", data);
        
        String customUsername = "customUser";
        String customPassword = "customPass";
        String customToken = "customToken123";
        
        puntoRedProperties.setUsername(customUsername);
        puntoRedProperties.setPassword(customPassword);
        
        when(authenticationService.getAuthenticationToken(customUsername, customPassword)).thenReturn(customToken);
        when(paymentGateway.processPayment(any(PaymentRequestDto.class), eq(customToken))).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        // Act
        ProcessPaymentResponse result = processPaymentUseCase.execute(request, USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getResponseCode()).isEqualTo(201);

        // Verify authentication was called with correct credentials
        verify(authenticationService).getAuthenticationToken(customUsername, customPassword);
        verify(paymentGateway).processPayment(any(PaymentRequestDto.class), eq(customToken));
    }

    @Test
    void testPaymentEntityCreation() {
        // Arrange
        ProcessPaymentRequest request = createProcessPaymentRequest();
        PaymentResponseDto.PaymentDataDto data = createSuccessfulPaymentData();
        PaymentResponseDto gatewayResponse = new PaymentResponseDto(201, "Payment processed successfully", data);
        
        when(paymentGateway.processPayment(any(PaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        // Act
        processPaymentUseCase.execute(request, USER_ID);

        // Assert - Verify successful payment entity creation
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getUserId()).isEqualTo(USER_ID);
        assertThat(savedPayment.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(AMOUNT));
        assertThat(savedPayment.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(savedPayment.getPaymentId()).isEqualTo(String.valueOf(PAYMENT_ID));
        assertThat(savedPayment.getReference()).isEqualTo(REFERENCE);
        assertThat(savedPayment.getResponseCode()).isEqualTo(201);
        assertThat(savedPayment.getResponseMessage()).isEqualTo("Payment processed successfully");
        assertThat(savedPayment.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void testFailedPaymentEntitySaving() {
        // Arrange
        ProcessPaymentRequest request = createProcessPaymentRequest();
        PaymentResponseDto.PaymentDataDto data = new PaymentResponseDto.PaymentDataDto(
                PAYMENT_ID, REFERENCE, AMOUNT, DESCRIPTION, "2024-01-01T10:00:00", "REJECTED", "Insufficient funds"
        );
        PaymentResponseDto gatewayResponse = new PaymentResponseDto(400, "Payment rejected", data);
        
        when(paymentGateway.processPayment(any(PaymentRequestDto.class), eq(VALID_TOKEN))).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        // Act & Assert
        assertThatThrownBy(() -> processPaymentUseCase.execute(request, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing payment: Payment rejected");

        // Verify failed payment entity saving with data from response
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getUserId()).isEqualTo(USER_ID);
        assertThat(savedPayment.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(AMOUNT));
        assertThat(savedPayment.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(savedPayment.getResponseCode()).isEqualTo(400);
        assertThat(savedPayment.getResponseMessage()).isEqualTo("Payment rejected");
        assertThat(savedPayment.getStatus()).isEqualTo("REJECTED"); // Status from data
        assertThat(savedPayment.getPaymentId()).isNull(); // Not set for failed payments
        assertThat(savedPayment.getReference()).isNull(); // Not set for failed payments
    }

    private ProcessPaymentRequest createProcessPaymentRequest() {
        return new ProcessPaymentRequest(EXTERNAL_ID, AMOUNT, DESCRIPTION, DUE_DATE, CALLBACK_URL);
    }

    private PaymentResponseDto.PaymentDataDto createSuccessfulPaymentData() {
        return new PaymentResponseDto.PaymentDataDto(
                PAYMENT_ID, REFERENCE, AMOUNT, DESCRIPTION, "2024-01-01T10:00:00", "PENDING", "Payment created successfully"
        );
    }
}