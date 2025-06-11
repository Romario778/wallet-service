package com.example.walletservice.controller;

import com.example.walletservice.dto.WalletOperationRequest;
import com.example.walletservice.dto.WalletResponse;
import com.example.walletservice.model.Wallet;
import com.example.walletservice.model.enums.OperationType;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class WalletControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("walletdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    private UUID walletId;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();

        walletId = UUID.randomUUID();

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .balance(1000L)
                .build();

        walletRepository.save(wallet);
    }

    @Test
    void testGetWalletBalance() {
        ResponseEntity<WalletResponse> response = restTemplate.getForEntity(
                "/wallets/" + walletId, WalletResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().balance()).isEqualTo(1000L);
    }

    @Test
    void testProcessOperationDeposit() {
        WalletOperationRequest request = new WalletOperationRequest(walletId, OperationType.DEPOSIT, 500L);
        ResponseEntity<WalletResponse> response = restTemplate.postForEntity("/wallets", request, WalletResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().balance()).isEqualTo(1500L);
    }
}
