package com.prs.worker.service;

import com.prs.documents.TransactionRequests;

public interface TransactionRequestService {

    TransactionRequests getTransactionRequest(String transactionRequestId);

    void save(TransactionRequests transactionRequests);
}
