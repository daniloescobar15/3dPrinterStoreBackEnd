package com.printerstore.backend.domain.payment.repository;

import com.printerstore.backend.domain.payment.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    
    Payment save(Payment payment);
    
    Optional<Payment> findByReference(String reference);
    
    List<Payment> findByExternalId(String externalId);
    
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);
}