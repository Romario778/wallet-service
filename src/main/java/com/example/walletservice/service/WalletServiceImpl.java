package com.example.walletservice.service;

import com.example.walletservice.dto.WalletOperationRequest;
import com.example.walletservice.dto.WalletResponse;
import com.example.walletservice.exception.WalletNotFoundException;
import com.example.walletservice.model.Wallet;
import com.example.walletservice.model.WalletTransaction;
import com.example.walletservice.model.enums.OperationType;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Cacheable(value = "wallets", key = "#walletId")
    @Override
    @Transactional()
    public WalletResponse getWalletBalance(UUID walletId) {
        return walletRepository.findWalletBalanceById(walletId)
                .map(balance -> new WalletResponse(walletId, balance))
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @CacheEvict(value = "wallets", key = "#request.walletId()")
    @Override
    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 1.5)
    )
    public WalletResponse processOperation(WalletOperationRequest request) {
        int updated = walletRepository.updateWalletBalance(
                request.walletId(),
                request.operationType() == OperationType.DEPOSIT ? request.amount() : -request.amount(),
                request.operationType() == OperationType.WITHDRAW ? request.amount() : 0
        );

        if (updated == 0) {
            throw new WalletNotFoundException(request.walletId());
        }

        saveTransactionAsync(request);

        Long balance = walletRepository.findWalletBalanceById(request.walletId())
                .orElseThrow(() -> new WalletNotFoundException(request.walletId()));

        return new WalletResponse(request.walletId(), balance);
    }

    @Async
    protected void saveTransactionAsync(WalletOperationRequest request) {
        WalletTransaction transaction = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .wallet(Wallet.builder().id(request.walletId()).build()) // Proxy entity
                .operationType(request.operationType())
                .amount(request.amount())
                .createdAt(Instant.now())
                .build();
        transactionRepository.save(transaction);
    }

    @Cacheable(value = "transactions", key = "#walletId")
    @Override
    @Transactional(readOnly = true)
    public List<WalletTransaction> getWalletTransactions(UUID walletId) {
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        transactions.forEach(t -> Hibernate.initialize(t.getWallet()));
        return transactions;
    }
}