package com.printerstore.backend.application.payment.usecase;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentCallbackUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private UpdatePaymentCallbackUseCase updatePaymentCallbackUseCase;

    private static final Long PAYMENT_ID = 12345L;
    private static final String EXTERNAL_ID = "EXT-12345";
    private static final Double AMOUNT = 100.50;
    private static final String AUTHORIZATION_NUMBER = "AUTH-67890";
    private static final String REFERENCE = "REF-12345";
    private static final Long PAYMENT_DATE = 1641024000000L;
    private static final String STATUS = "APPROVED";
    private static final String MESSAGE = "Payment successful";

    @BeforeEach
    void setUp() {
        reset(paymentRepository);
    }

    @Test
    void testPaymentFoundByReference() {
        // Arrange
        Payment existingPayment = createExistingPayment();
        
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        updatePaymentCallbackUseCase.execute(PAYMENT_ID, EXTERNAL_ID, AMOUNT, 
                AUTHORIZATION_NUMBER, REFERENCE, PAYMENT_DATE, STATUS, MESSAGE);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getPaymentId()).isEqualTo(String.valueOf(PAYMENT_ID));
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(AMOUNT));
        assertThat(savedPayment.getReference()).isEqualTo(REFERENCE);
        assertThat(savedPayment.getStatus()).isEqualTo(STATUS);
        assertThat(savedPayment.getResponseMessage()).isEqualTo(MESSAGE);
        assertThat(savedPayment.getResponseCode()).isEqualTo(200);

        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository, never()).findByExternalId(anyString());
    }

    @Test
    void testPaymentFoundByExternalId() {
        // Arrange
        Payment existingPayment = createExistingPayment();
        
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.empty());
        when(paymentRepository.findByExternalId(EXTERNAL_ID)).thenReturn(Collections.singletonList(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        updatePaymentCallbackUseCase.execute(PAYMENT_ID, EXTERNAL_ID, AMOUNT, 
                AUTHORIZATION_NUMBER, REFERENCE, PAYMENT_DATE, STATUS, MESSAGE);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getPaymentId()).isEqualTo(String.valueOf(PAYMENT_ID));
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(AMOUNT));
        assertThat(savedPayment.getReference()).isEqualTo(REFERENCE);
        assertThat(savedPayment.getStatus()).isEqualTo(STATUS);
        assertThat(savedPayment.getResponseMessage()).isEqualTo(MESSAGE);
        assertThat(savedPayment.getResponseCode()).isEqualTo(200);

        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository).findByExternalId(EXTERNAL_ID);
    }

    @Test
    void testPaymentFoundByExternalIdMultiple() {
        // Arrange
        Payment olderPayment = createExistingPayment();
        olderPayment.setCreatedAt(LocalDateTime.now().minusDays(2));
        
        Payment newerPayment = createExistingPayment();
        newerPayment.setCreatedAt(LocalDateTime.now().minusDays(1));
        
        List<Payment> payments = Arrays.asList(olderPayment, newerPayment);
        
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.empty());
        when(paymentRepository.findByExternalId(EXTERNAL_ID)).thenReturn(payments);
        when(paymentRepository.save(any(Payment.class))).thenReturn(newerPayment);

        // Act
        updatePaymentCallbackUseCase.execute(PAYMENT_ID, EXTERNAL_ID, AMOUNT, 
                AUTHORIZATION_NUMBER, REFERENCE, PAYMENT_DATE, STATUS, MESSAGE);

        // Assert - Should update the newer payment (latest createdAt)
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getCreatedAt()).isEqualTo(newerPayment.getCreatedAt());
        assertThat(savedPayment.getPaymentId()).isEqualTo(String.valueOf(PAYMENT_ID));
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(AMOUNT));
        assertThat(savedPayment.getReference()).isEqualTo(REFERENCE);
        assertThat(savedPayment.getStatus()).isEqualTo(STATUS);
        assertThat(savedPayment.getResponseMessage()).isEqualTo(MESSAGE);
        assertThat(savedPayment.getResponseCode()).isEqualTo(200);

        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository).findByExternalId(EXTERNAL_ID);
    }

    @Test
    void testPaymentNotFoundAnywhere() {
        // Arrange
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.empty());
        when(paymentRepository.findByExternalId(EXTERNAL_ID)).thenReturn(Collections.emptyList());

        // Act
        updatePaymentCallbackUseCase.execute(PAYMENT_ID, EXTERNAL_ID, AMOUNT, 
                AUTHORIZATION_NUMBER, REFERENCE, PAYMENT_DATE, STATUS, MESSAGE);

        // Assert
        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository).findByExternalId(EXTERNAL_ID);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testNullExternalIdParameter() {
        // Arrange
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.empty());

        // Act
        updatePaymentCallbackUseCase.execute(PAYMENT_ID, null, AMOUNT, 
                AUTHORIZATION_NUMBER, REFERENCE, PAYMENT_DATE, STATUS, MESSAGE);

        // Assert
        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository, never()).findByExternalId(anyString());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testEmptyExternalIdParameter() {
        // Arrange
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.empty());

        // Act
        updatePaymentCallbackUseCase.execute(PAYMENT_ID, "", AMOUNT, 
                AUTHORIZATION_NUMBER, REFERENCE, PAYMENT_DATE, STATUS, MESSAGE);

        // Assert
        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository, never()).findByExternalId(anyString());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testNullAmountParameter() {
        // Arrange
        Payment existingPayment = createExistingPayment();
        
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.of(existingPayment));

        // Act & Assert
        assertThatThrownBy(() -> updatePaymentCallbackUseCase.execute(PAYMENT_ID, EXTERNAL_ID, null, 
                AUTHORIZATION_NUMBER, REFERENCE, PAYMENT_DATE, STATUS, MESSAGE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Cannot invoke \"java.lang.Double.doubleValue()\" because \"amount\" is null");

        verify(paymentRepository).findByReference(REFERENCE);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testRepositorySaveMethodCalled() {
        // Arrange
        Payment existingPayment = createExistingPayment();
        Payment savedPayment = createExistingPayment();
        savedPayment.setStatus(STATUS);
        
        when(paymentRepository.findByReference(REFERENCE)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        updatePaymentCallbackUseCase.execute(PAYMENT_ID, EXTERNAL_ID, AMOUNT, 
                AUTHORIZATION_NUMBER, REFERENCE, PAYMENT_DATE, STATUS, MESSAGE);

        // Assert
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentRepository).findByReference(REFERENCE);
        
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment).isNotNull();
        assertThat(capturedPayment.getStatus()).isEqualTo(STATUS);
        assertThat(capturedPayment.getResponseCode()).isEqualTo(200);
    }

    private Payment createExistingPayment() {
        return Payment.builder()
                .id(1L)
                .userId("user123")
                .externalId("EXT-OLD")
                .amount(BigDecimal.valueOf(50.00))
                .paymentId("OLD-PAYMENT-ID")
                .reference("OLD-REF")
                .responseCode(100)
                .responseMessage("Pending")
                .status("PENDING")
                .description("Test payment")
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
    }
}