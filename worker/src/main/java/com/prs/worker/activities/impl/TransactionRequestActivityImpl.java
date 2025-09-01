package com.prs.worker.activities.impl;

import com.prs.constans.TransactionStatus;
import com.prs.dao.TransactionRequestRepository;
import com.prs.worker.activities.TransactionRequestActivity;
import io.temporal.failure.ApplicationFailure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("TransactionRequestActivityImpl")
@Slf4j
public class TransactionRequestActivityImpl implements TransactionRequestActivity {

    private final TransactionRequestRepository transactionRequestRepository;

    public TransactionRequestActivityImpl(TransactionRequestRepository transactionRequestRepository) {
        this.transactionRequestRepository = transactionRequestRepository;
    }

    @Override
    public void markTransactionRequestInProgress(String transactionRequestId) {
        transactionRequestRepository.findById(transactionRequestId)
                .map(doc -> {
                    if (doc.getTransactionStatus() != TransactionStatus.IN_PROGRESS) {
                        doc.setTransactionStatus(TransactionStatus.IN_PROGRESS);
                        transactionRequestRepository.save(doc);
                    }
                    return doc;
                })
                .orElseThrow(() -> ApplicationFailure.newFailure(String.format("%s transaction request not found", transactionRequestId), "TransactionRequestNotFound"));
    }

    @Override
    public void markTransactionRequestSucceeded(String transactionRequestId) {
        transactionRequestRepository.findById(transactionRequestId)
                .map(doc -> {
                    if (doc.getTransactionStatus() == TransactionStatus.IN_PROGRESS) {
                        doc.setTransactionStatus(TransactionStatus.COMPLETED);
                        transactionRequestRepository.save(doc);
                    }
                    return doc;
                })
                .orElseThrow(() -> ApplicationFailure.newNonRetryableFailure(String.format("%s transaction request not found", transactionRequestId), "TransactionRequestNotFound"));
    }

    @Override
    public void markTransactionRequestFailed(String transactionRequestId) {
        transactionRequestRepository.findById(transactionRequestId)
                .map(doc -> {
                    if (doc.getTransactionStatus() == TransactionStatus.IN_PROGRESS) {
                        doc.setTransactionStatus(TransactionStatus.FAILED);
                        transactionRequestRepository.save(doc);
                    }
                    return doc;
                })
                .orElseThrow(() -> ApplicationFailure.newNonRetryableFailure(String.format("%s transaction request not found", transactionRequestId), "TransactionRequestNotFound"));
    }

    @Override
    public void storePatientState(String transactionRequestId, String patientId) {

    }

    @Override
    public void storeMedicalRecordState(String transactionRequestId, String medicalRecordId) {

    }

    @Override
    public void storeInsuranceDetailsState(String transactionRequestId, String insuranceDetailId) {

    }
}
