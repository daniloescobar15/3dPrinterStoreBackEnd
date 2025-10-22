package com.printerstore.backend.infrastructure.persistence;

import com.printerstore.backend.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
    
    Optional<PaymentEntity> findByReference(String reference);
    
    List<PaymentEntity> findByExternalId(String externalId);
    
    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}