package com.example.walletservice.service;

import com.example.walletservice.dto.WalletOperationRequest;
import com.example.walletservice.dto.WalletResponse;
import com.example.walletservice.model.WalletTransaction;

import java.util.List;
import java.util.UUID;

public interface WalletService {
    WalletResponse getWalletBalance(UUID walletId);

    WalletResponse processOperation(WalletOperationRequest request);

    List<WalletTransaction> getWalletTransactions(UUID walletId);
}