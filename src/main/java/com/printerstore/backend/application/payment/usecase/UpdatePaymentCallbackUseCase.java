package com.printerstore.backend.application.payment.usecase;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentCallbackUseCase {

    private final PaymentRepository paymentRepository;

    public void execute(Long paymentId, String externalId, Double amount, 
                       String authorizationNumber, String reference, 
                       Long paymentDate, String status, String message) {
        log.info("Payment callback received. Reference: {}, ExternalId: {}, PaymentId: {}, Status: {}",
                reference, externalId, paymentId, status);
        
        Optional<Payment> paymentOptional = paymentRepository.findByReference(reference);
        
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            payment.setPaymentId(String.valueOf(paymentId));
            payment.setAmount(BigDecimal.valueOf(amount));
            payment.setReference(reference);
            payment.setStatus(status);
            payment.setResponseMessage(message);
            payment.setResponseCode(200);
            
            paymentRepository.save(payment);
            log.info("Payment successfully updated from callback. PaymentId: {}, Reference: {}, Status: {}",
                    payment.getId(), reference, status);
        } else {
            log.warn("Payment not found with reference: {}. Searching by externalId: {}", 
                    reference, externalId);
            
            if (externalId != null && !externalId.isEmpty()) {
                List<Payment> payments = paymentRepository.findByExternalId(externalId);
                
                if (!payments.isEmpty()) {
                    Payment payment = payments.stream()
                            .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()))
                            .orElse(payments.get(0));
                    
                    payment.setPaymentId(String.valueOf(paymentId));
                    payment.setAmount(BigDecimal.valueOf(amount));
                    payment.setReference(reference);
                    payment.setStatus(status);
                    payment.setResponseMessage(message);
                    payment.setResponseCode(200);
                    
                    paymentRepository.save(payment);
                    log.info("Payment updated (search by externalId) from callback. PaymentId: {}, Reference: {}, Status: {}",
                            payment.getId(), reference, status);
                } else {
                    log.warn("Payment not found with reference: {} or externalId: {} to update from callback",
                            reference, externalId);
                }
            } else {
                log.warn("Payment not found with reference: {} and externalId is null", reference);
            }
        }
    }
}