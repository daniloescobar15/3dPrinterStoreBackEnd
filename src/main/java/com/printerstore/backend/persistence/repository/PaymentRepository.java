package com.printerstore.backend.persistence.repository;

import com.printerstore.backend.persistence.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Encuentra todos los pagos por su externalId
     */
    List<Payment> findByExternalId(String externalId);

    /**
     * Encuentra todos los pagos de un usuario
     */
    List<Payment> findByUserId(String userId);

    /**
     * Encuentra todos los pagos de un usuario ordenados por fecha de creación descendente
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Encuentra todos los pagos de un usuario con un estado específico
     */
    List<Payment> findByUserIdAndStatus(String userId, String status);

    /**
     * Encuentra un pago por su reference
     */
    Optional<Payment> findByReference(String reference);
}