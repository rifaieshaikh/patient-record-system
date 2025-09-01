package com.prs.worker.workflows;

import com.prs.worker.activities.CreatePatientActivity;
import com.prs.worker.activities.TransactionRequestActivity;
import com.prs.workflow.CreatePatientWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;


import java.time.Duration;

@Slf4j
public class CreatePatientWorkflowImpl implements CreatePatientWorkflow {

    private final RetryOptions patRetry = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1))
            .setBackoffCoefficient(2.0)
            .setMaximumInterval(Duration.ofSeconds(30))
            .setMaximumAttempts(10)
            .build();

    private final RetryOptions tranRetry = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1))
            .setBackoffCoefficient(2.0)
            .setMaximumInterval(Duration.ofSeconds(30))
            .setMaximumAttempts(5)
            .build();

    private final ActivityOptions patOpt = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(20))
            .setRetryOptions(patRetry)
            .build();

    private final ActivityOptions tranOpt = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(tranRetry)
            .build();

    private final CreatePatientActivity createPatientActivity = Workflow.newActivityStub(CreatePatientActivity.class, patOpt);
    private final TransactionRequestActivity transactionRequestActivity = Workflow.newActivityStub(TransactionRequestActivity.class, tranOpt);


    @Override
    public void createPatient(String requestId) {
        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());
        log.info("Create Patient Workflow Started");
        try {
            transactionRequestActivity.markTransactionRequestInProgress(requestId);

            createPatientActivity.createPatientRecord(requestId);
            saga.addCompensation(() -> createPatientActivity.rollbackPatientRecord(requestId));

            createPatientActivity.createMedicalRecord(requestId);
            saga.addCompensation(() -> createPatientActivity.rollbackMedicalRecord(requestId));

            createPatientActivity.createInsuranceRecord(requestId);
            saga.addCompensation(() -> createPatientActivity.rollbackInsuranceRecord(requestId));

            transactionRequestActivity.markTransactionRequestSucceeded(requestId);
            log.info("Create Patient Workflow Completed");
        } catch (ActivityFailure af) {
            saga.compensate();
            try {
                transactionRequestActivity.markTransactionRequestFailed(requestId);
            } catch (ActivityFailure af2) {}
            log.info("Create Patient Workflow Failed");
            throw af;
        }
    }
}
