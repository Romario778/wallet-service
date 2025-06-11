package com.example.walletservice.exception;

import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    private final UUID walletId;
    private final long currentBalance;
    private final long requiredAmount;

    public InsufficientFundsException(UUID walletId, long currentBalance, long requiredAmount) {
        super(String.format(
                "Insufficient funds in wallet %s. Current balance: %d, required amount: %d",
                walletId, currentBalance, requiredAmount
        ));
        this.walletId = walletId;
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public long getCurrentBalance() {
        return currentBalance;
    }

    public long getRequiredAmount() {
        return requiredAmount;
    }
}