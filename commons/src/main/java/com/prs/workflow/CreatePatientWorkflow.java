package com.prs.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface CreatePatientWorkflow {

    @WorkflowMethod
    void createPatient(String requestId);
}
