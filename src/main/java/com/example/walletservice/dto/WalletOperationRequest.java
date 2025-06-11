package com.example.walletservice.dto;

import com.example.walletservice.model.enums.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record WalletOperationRequest(
        @NotNull UUID walletId,
        @NotNull OperationType operationType,
        @Positive long amount
) {
}