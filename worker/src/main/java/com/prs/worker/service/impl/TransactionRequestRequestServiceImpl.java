package com.prs.worker.service.impl;

import com.prs.dao.TransactionRequestRepository;
import com.prs.documents.TransactionRequests;
import com.prs.worker.service.TransactionRequestService;
import io.temporal.failure.ApplicationFailure;
import org.springframework.stereotype.Service;

@Service
public class TransactionRequestRequestServiceImpl implements TransactionRequestService {
    private final TransactionRequestRepository  transactionRequestRepository;

    public TransactionRequestRequestServiceImpl(TransactionRequestRepository transactionRequestRepository) {
        this.transactionRequestRepository = transactionRequestRepository;
    }

    @Override
    public TransactionRequests getTransactionRequest(String transactionRequestId) {
        return transactionRequestRepository.findById(transactionRequestId)
                .orElseThrow(() -> ApplicationFailure.newNonRetryableFailure(String.format("%s transaction request not found", transactionRequestId), "TransactionRequestNotFound"));
    }

    @Override
    public void save(TransactionRequests transactionRequests) {
        transactionRequestRepository.save(transactionRequests);
    }
}
