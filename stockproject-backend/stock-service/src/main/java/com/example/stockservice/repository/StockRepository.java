package com.example.stockservice.repository;

import com.example.stockservice.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 해당 productId를 가진 행에 락을 건다.
    Optional<Stock> findByProductId(Long productId);
}