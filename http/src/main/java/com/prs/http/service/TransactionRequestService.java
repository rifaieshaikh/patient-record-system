package com.prs.http.service;

import com.prs.dtos.TransactionRequestDTO;
import com.prs.http.dto.TransactionResponse;

public interface TransactionRequestService {
    TransactionResponse create(TransactionRequestDTO request);
}
