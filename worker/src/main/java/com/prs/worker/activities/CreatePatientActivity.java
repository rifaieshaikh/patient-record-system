package com.prs.worker.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface CreatePatientActivity {
    void createPatientRecord(String transactionRequestId);
    void rollbackPatientRecord(String transactionRequestId);

    void createMedicalRecord(String transactionRequestId);
    void rollbackMedicalRecord(String transactionRequestId);

    void createInsuranceRecord(String transactionRequestId);
    void rollbackInsuranceRecord(String transactionRequestId);
}
