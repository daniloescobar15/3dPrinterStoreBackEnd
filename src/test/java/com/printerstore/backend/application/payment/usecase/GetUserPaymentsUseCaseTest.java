package com.printerstore.backend.application.payment.usecase;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserPaymentsUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    private GetUserPaymentsUseCase getUserPaymentsUseCase;

    private static final String VALID_USER_ID = "user123";
    private static final String EMPTY_USER_ID = "";
    private static final String NULL_USER_ID = null;

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test
        reset(paymentRepository);
        
        // Create the use case with the mocked repository
        getUserPaymentsUseCase = new GetUserPaymentsUseCase(paymentRepository);
    }

    @Test
    void testSuccessfulGetUserPaymentsWithMultiplePayments() {
        // Arrange
        List<Payment> expectedPayments = createMultiplePayments();
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(VALID_USER_ID))
                .thenReturn(expectedPayments);

        // Act
        List<Payment> result = getUserPaymentsUseCase.execute(VALID_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(expectedPayments);
        
        // Verify repository interaction
        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(VALID_USER_ID);
    }

    @Test
    void testSuccessfulGetUserPaymentsWithSinglePayment() {
        // Arrange
        List<Payment> expectedPayments = createSinglePayment();
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(VALID_USER_ID))
                .thenReturn(expectedPayments);

        // Act
        List<Payment> result = getUserPaymentsUseCase.execute(VALID_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(VALID_USER_ID);
        assertThat(result.get(0).getAmount()).isEqualTo(new BigDecimal("100.00"));
        
        // Verify repository interaction
        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(VALID_USER_ID);
    }

    @Test
    void testSuccessfulGetUserPaymentsWithEmptyResult() {
        // Arrange
        List<Payment> emptyPayments = Collections.emptyList();
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(VALID_USER_ID))
                .thenReturn(emptyPayments);

        // Act
        List<Payment> result = getUserPaymentsUseCase.execute(VALID_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        // Verify repository interaction
        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(VALID_USER_ID);
    }

    @Test
    void testGetUserPaymentsWithEmptyUserId() {
        // Arrange
        List<Payment> emptyPayments = Collections.emptyList();
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(EMPTY_USER_ID))
                .thenReturn(emptyPayments);

        // Act
        List<Payment> result = getUserPaymentsUseCase.execute(EMPTY_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        // Verify repository interaction
        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(EMPTY_USER_ID);
    }

    @Test
    void testGetUserPaymentsWithNullUserId() {
        // Arrange
        List<Payment> emptyPayments = Collections.emptyList();
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(NULL_USER_ID))
                .thenReturn(emptyPayments);

        // Act
        List<Payment> result = getUserPaymentsUseCase.execute(NULL_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        // Verify repository interaction
        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(NULL_USER_ID);
    }

    @Test
    void testRepositoryThrowsRuntimeException() {
        // Arrange
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(VALID_USER_ID))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThatThrownBy(() -> getUserPaymentsUseCase.execute(VALID_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection error");

        // Verify repository interaction
        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(VALID_USER_ID);
    }

    @Test
    void testRepositoryReturnsOrderedPayments() {
        // Arrange - Create payments with different timestamps to verify ordering
        LocalDateTime now = LocalDateTime.now();
        Payment payment1 = createPaymentWithTimestamp("payment1", now.minusHours(1));
        Payment payment2 = createPaymentWithTimestamp("payment2", now.minusHours(2));
        Payment payment3 = createPaymentWithTimestamp("payment3", now);
        
        // Repository should return them in descending order (newest first)
        List<Payment> orderedPayments = Arrays.asList(payment3, payment1, payment2);
        
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(VALID_USER_ID))
                .thenReturn(orderedPayments);

        // Act
        List<Payment> result = getUserPaymentsUseCase.execute(VALID_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        // Verify the order is maintained (newest first)
        assertThat(result.get(0).getReference()).isEqualTo("payment3");
        assertThat(result.get(1).getReference()).isEqualTo("payment1");
        assertThat(result.get(2).getReference()).isEqualTo("payment2");
        
        // Verify repository interaction
        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(VALID_USER_ID);
    }

    @Test
    void testRepositoryMethodCalledExactlyOnce() {
        // Arrange
        List<Payment> payments = createMultiplePayments();
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(VALID_USER_ID))
                .thenReturn(payments);

        // Act
        getUserPaymentsUseCase.execute(VALID_USER_ID);

        // Assert - Verify the method was called exactly once
        verify(paymentRepository, times(1)).findByUserIdOrderByCreatedAtDesc(VALID_USER_ID);
        verifyNoMoreInteractions(paymentRepository);
    }

    // Helper methods for creating test data

    private List<Payment> createMultiplePayments() {
        LocalDateTime now = LocalDateTime.now();
        
        Payment payment1 = Payment.builder()
                .id(1L)
                .userId(VALID_USER_ID)
                .externalId("ext-1")
                .amount(new BigDecimal("100.00"))
                .paymentId("pay-1")
                .reference("REF-001")
                .responseCode(200)
                .responseMessage("Success")
                .status("01")
                .description("First payment")
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(2))
                .build();
                
        Payment payment2 = Payment.builder()
                .id(2L)
                .userId(VALID_USER_ID)
                .externalId("ext-2")
                .amount(new BigDecimal("250.50"))
                .paymentId("pay-2")
                .reference("REF-002")
                .responseCode(200)
                .responseMessage("Success")
                .status("01")
                .description("Second payment")
                .createdAt(now.minusHours(1))
                .updatedAt(now.minusHours(1))
                .build();
                
        Payment payment3 = Payment.builder()
                .id(3L)
                .userId(VALID_USER_ID)
                .externalId("ext-3")
                .amount(new BigDecimal("75.25"))
                .paymentId("pay-3")
                .reference("REF-003")
                .responseCode(200)
                .responseMessage("Success")
                .status("01")
                .description("Third payment")
                .createdAt(now)
                .updatedAt(now)
                .build();
                
        return Arrays.asList(payment1, payment2, payment3);
    }

    private List<Payment> createSinglePayment() {
        LocalDateTime now = LocalDateTime.now();
        
        Payment payment = Payment.builder()
                .id(1L)
                .userId(VALID_USER_ID)
                .externalId("ext-1")
                .amount(new BigDecimal("100.00"))
                .paymentId("pay-1")
                .reference("REF-001")
                .responseCode(200)
                .responseMessage("Success")
                .status("01")
                .description("Single payment")
                .createdAt(now)
                .updatedAt(now)
                .build();
                
        return Collections.singletonList(payment);
    }

    private Payment createPaymentWithTimestamp(String reference, LocalDateTime timestamp) {
        return Payment.builder()
                .id(1L)
                .userId(VALID_USER_ID)
                .externalId("ext-" + reference)
                .amount(new BigDecimal("100.00"))
                .paymentId("pay-" + reference)
                .reference(reference)
                .responseCode(200)
                .responseMessage("Success")
                .status("01")
                .description("Payment for " + reference)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }
}