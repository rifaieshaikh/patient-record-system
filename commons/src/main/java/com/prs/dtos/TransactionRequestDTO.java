package com.prs.dtos;

import com.prs.constans.TransactionType;
import com.prs.documents.TransactionRequests;
import jakarta.validation.constraints.NotNull;

public record TransactionRequestDTO(@NotNull PatientDto patient, @NotNull TransactionType  transactionType) {

    public TransactionRequests getTransactionRequest() {
        TransactionRequests transactionRequests = new TransactionRequests();
        transactionRequests.setTransactionType(transactionType);
        transactionRequests.setPayload(this);
        return transactionRequests;
    }
}
