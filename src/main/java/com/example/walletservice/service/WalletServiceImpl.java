package com.example.walletservice.service;

import com.example.walletservice.dto.WalletOperationRequest;
import com.example.walletservice.dto.WalletResponse;
import com.example.walletservice.exception.InsufficientFundsException;
import com.example.walletservice.exception.WalletNotFoundException;
import com.example.walletservice.model.Wallet;
import com.example.walletservice.model.WalletTransaction;
import com.example.walletservice.model.enums.OperationType;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public WalletResponse processOperation(WalletOperationRequest request) {
        Wallet wallet = walletRepository.findByIdForUpdate(request.walletId())
                .orElseThrow(() -> new WalletNotFoundException(request.walletId()));

        long newBalance = calculateNewBalance(wallet, request);
        wallet.setBalance(newBalance);

        Wallet savedWallet = walletRepository.save(wallet);
        saveTransaction(wallet, request);

        return new WalletResponse(savedWallet.getId(), savedWallet.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransaction> getWalletTransactions(UUID walletId) {
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        transactions.forEach(t -> Hibernate.initialize(t.getWallet()));
        return transactions;
    }

    private long calculateNewBalance(Wallet wallet, WalletOperationRequest request) {
        if (request.operationType() == OperationType.DEPOSIT) {
            return wallet.getBalance() + request.amount();
        }

        if (wallet.getBalance() < request.amount()) {
            throw new InsufficientFundsException(
                    request.walletId(),
                    wallet.getBalance(),
                    request.amount()
            );
        }
        return wallet.getBalance() - request.amount();
    }

    private void saveTransaction(Wallet wallet, WalletOperationRequest request) {
        WalletTransaction transaction = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .operationType(request.operationType())
                .amount(request.amount())
                .createdAt(Instant.now())
                .build();
        transactionRepository.save(transaction);
    }
}