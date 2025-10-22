package com.printerstore.backend.application.payment.port;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.application.payment.dto.ProcessPaymentRequest;
import com.printerstore.backend.application.payment.dto.ProcessPaymentResponse;
import com.printerstore.backend.application.payment.dto.CancelPaymentResponse;

import java.util.List;

public interface PaymentServicePort {
    
    ProcessPaymentResponse processPayment(ProcessPaymentRequest request, String userId);
    
    List<Payment> getUserPayments(String userId);
    
    void updatePaymentCallback(Long paymentId, String externalId, Double amount, 
                               String authorizationNumber, String reference, 
                               Long paymentDate, String status, String message);
    
    CancelPaymentResponse cancelPayment(String reference, String cancelDescription);
}