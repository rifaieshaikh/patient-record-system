package com.prs.http.api;

import com.prs.dtos.TransactionRequestDTO;
import com.prs.http.dto.TransactionResponse;
import com.prs.http.service.TransactionRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionRequestController {

    private final TransactionRequestService transactionRequestService;

    public TransactionRequestController(TransactionRequestService transactionRequestService) {
        this.transactionRequestService = transactionRequestService;
    }

    @PostMapping("/create")
    public ResponseEntity<TransactionResponse> createPatient(@Valid @RequestBody TransactionRequestDTO request) {
        return ResponseEntity.ok(transactionRequestService.create(request));
    }
}
