package com.printerstore.backend.infrastructure.persistence.adapter;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import com.printerstore.backend.infrastructure.persistence.PaymentJpaRepository;
import com.printerstore.backend.infrastructure.persistence.entity.PaymentEntity;
import com.printerstore.backend.infrastructure.persistence.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = paymentMapper.toPersistenceEntity(payment);
        PaymentEntity saved = jpaRepository.save(entity);
        return paymentMapper.toDomainEntity(saved);
    }

    @Override
    public Optional<Payment> findByReference(String reference) {
        return jpaRepository.findByReference(reference)
                .map(paymentMapper::toDomainEntity);
    }

    @Override
    public List<Payment> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId)
                .stream()
                .map(paymentMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByUserIdOrderByCreatedAtDesc(String userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(paymentMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}