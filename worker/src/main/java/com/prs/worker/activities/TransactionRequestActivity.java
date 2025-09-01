package com.prs.worker.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface TransactionRequestActivity {
    void markTransactionRequestInProgress(String transactionRequestId);
    void markTransactionRequestSucceeded(String transactionRequestId);
    void markTransactionRequestFailed(String transactionRequestId);
    void storePatientState(String transactionRequestId, String patientId);
    void storeMedicalRecordState(String transactionRequestId, String medicalRecordId);
    void storeInsuranceDetailsState(String transactionRequestId, String insuranceDetailId);
}
