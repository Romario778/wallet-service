package com.example.walletservice.dto;

import java.util.UUID;

public record WalletResponse(
        UUID walletId,
        long balance
) {
}