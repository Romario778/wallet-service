package com.example.walletservice.controller;

import com.example.walletservice.dto.WalletOperationRequest;
import com.example.walletservice.dto.WalletResponse;
import com.example.walletservice.model.WalletTransaction;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> processOperation(
            @RequestBody @Valid WalletOperationRequest request) {
        return ResponseEntity.ok(walletService.processOperation(request));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWalletBalance(
            @PathVariable UUID walletId) {
        return ResponseEntity.ok(walletService.getWalletBalance(walletId));
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<WalletTransaction>> getWalletTransactions(
            @PathVariable UUID walletId) {
        return ResponseEntity.ok(walletService.getWalletTransactions(walletId));
    }
}
