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
        log.info("Recibido callback de pago. Reference: {}, ExternalId: {}, PaymentId: {}, Status: {}",
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
            log.info("Pago actualizado exitosamente desde callback. PaymentId: {}, Reference: {}, Status: {}",
                    payment.getId(), reference, status);
        } else {
            log.warn("No se encontró pago con reference: {}. Buscando por externalId: {}", 
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
                    log.info("Pago actualizado (búsqueda por externalId) desde callback. PaymentId: {}, Reference: {}, Status: {}",
                            payment.getId(), reference, status);
                } else {
                    log.warn("No se encontró pago con reference: {} ni con externalId: {} para actualizar desde callback",
                            reference, externalId);
                }
            } else {
                log.warn("No se encontró pago con reference: {} y externalId es nulo", reference);
            }
        }
    }
}