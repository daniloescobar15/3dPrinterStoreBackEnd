package com.printerstore.backend.application.payment.usecase;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserPaymentsUseCase {

    private final PaymentRepository paymentRepository;

    public List<Payment> execute(String userId) {
        log.info("Obteniendo pagos para userId: {}", userId);
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Se encontraron {} pagos para userId: {}", payments.size(), userId);
        return payments;
    }
}