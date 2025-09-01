package com.prs.http.service.impl;

import com.prs.constans.TransactionStatus;
import com.prs.dao.TransactionRequestRepository;
import com.prs.documents.TransactionRequests;
import com.prs.dtos.TransactionRequestDTO;
import com.prs.http.dto.TransactionResponse;
import com.prs.http.service.TransactionRequestService;
import com.prs.http.utils.WorkflowUtils;
import com.prs.workflow.CreatePatientWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransactionRequestServiceImpl implements TransactionRequestService {

    private final TransactionRequestRepository transactionRequestRepository;
    private final WorkflowClient workflowClient;
    private final String taskQueue;
    private final long workflowTimeoutSeconds;

    public TransactionRequestServiceImpl(TransactionRequestRepository transactionRequestRepository, WorkflowClient workflowClient,
                                         @Value("${temporal.taskQueue}") String taskQueue,
                                         @Value("${temporal.workflowTimeoutSeconds}") long workflowTimeoutSeconds) {
        this.transactionRequestRepository = transactionRequestRepository;
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
        this.workflowTimeoutSeconds = workflowTimeoutSeconds;
    }

    @Override
    public TransactionResponse create(TransactionRequestDTO request) {
        TransactionRequests transactionRequest = request.getTransactionRequest();
        transactionRequest.setTransactionStatus(TransactionStatus.RECEIVED);
        transactionRequest = transactionRequestRepository.save(transactionRequest);

        WorkflowOptions workflowOptions = WorkflowUtils.getOptions("create", taskQueue, workflowTimeoutSeconds);
        CreatePatientWorkflow createPatientWorkflow = workflowClient.newWorkflowStub(CreatePatientWorkflow.class, workflowOptions);
        WorkflowClient.start(createPatientWorkflow::createPatient, transactionRequest.getId());

        transactionRequest.setWorkflowId(workflowOptions.getWorkflowId());
        transactionRequest.setTransactionStatus(TransactionStatus.INITIATED);
        transactionRequest = transactionRequestRepository.save(transactionRequest);
        return new TransactionResponse(transactionRequest.getId(), transactionRequest.getWorkflowId(), transactionRequest.getTransactionStatus());
    }
}
