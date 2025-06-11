package com.example.walletservice.repository;

import com.example.walletservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w.balance FROM Wallet w WHERE w.id = :id")
    Optional<Long> findWalletBalanceById(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount " +
            "WHERE w.id = :id AND (:minBalanceRequired = 0 OR w.balance >= :minBalanceRequired)")
    int updateWalletBalance(
            @Param("id") UUID id,
            @Param("amount") long amount,
            @Param("minBalanceRequired") long minBalanceRequired);
}