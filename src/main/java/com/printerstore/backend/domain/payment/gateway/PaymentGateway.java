package com.printerstore.backend.domain.payment.gateway;

import com.printerstore.backend.domain.payment.gateway.dto.PaymentRequestDto;
import com.printerstore.backend.domain.payment.gateway.dto.PaymentResponseDto;
import com.printerstore.backend.domain.payment.gateway.dto.CancelPaymentRequestDto;
import com.printerstore.backend.domain.payment.gateway.dto.CancelPaymentResponseDto;

public interface PaymentGateway {
    
    PaymentResponseDto processPayment(PaymentRequestDto request, String token);
    
    CancelPaymentResponseDto cancelPayment(CancelPaymentRequestDto request, String token);
}