package com.printerstore.backend.infrastructure.adapter.in;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.application.payment.port.PaymentServicePort;
import com.printerstore.backend.application.payment.dto.ProcessPaymentRequest;
import com.printerstore.backend.application.payment.dto.ProcessPaymentResponse;
import com.printerstore.backend.application.payment.dto.CancelPaymentResponse;
import com.printerstore.backend.application.payment.usecase.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceAdapter implements PaymentServicePort {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final GetUserPaymentsUseCase getUserPaymentsUseCase;
    private final UpdatePaymentCallbackUseCase updatePaymentCallbackUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;

    @Override
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request, String userId) {
        return processPaymentUseCase.execute(request, userId);
    }

    @Override
    public List<Payment> getUserPayments(String userId) {
        return getUserPaymentsUseCase.execute(userId);
    }

    @Override
    public void updatePaymentCallback(Long paymentId, String externalId, Double amount, 
                                      String authorizationNumber, String reference, 
                                      Long paymentDate, String status, String message) {
        updatePaymentCallbackUseCase.execute(paymentId, externalId, amount, authorizationNumber, reference, paymentDate, status, message);
    }

    @Override
    public CancelPaymentResponse cancelPayment(String reference, String cancelDescription) {
        return cancelPaymentUseCase.execute(reference, cancelDescription);
    }
}