package com.prs.http.dto;

import com.prs.constans.TransactionStatus;

public record TransactionResponse(String requestId, String workflowId, TransactionStatus status) {
}
