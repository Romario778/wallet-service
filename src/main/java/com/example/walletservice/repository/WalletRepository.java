package com.example.walletservice.repository;

import com.example.walletservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Override
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Wallet> findById(UUID id);

    @Query("SELECT w.balance FROM Wallet w WHERE w.id = :walletId")
    Optional<Long> findBalanceById(@Param("walletId") UUID walletId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :walletId")
    Optional<Wallet> findByIdForUpdate(@Param("walletId") UUID walletId);
}