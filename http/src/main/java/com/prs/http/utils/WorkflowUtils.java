package com.prs.http.utils;

import io.temporal.client.WorkflowOptions;

import java.time.Duration;
import java.util.UUID;

public class WorkflowUtils {

    public static WorkflowOptions getOptions(String prefix, String taskQueue, Long workflowTimeoutSeconds) {
        String workflowId = prefix + "-" + UUID.randomUUID().toString();
        return WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId(workflowId)
                .setWorkflowRunTimeout(Duration.ofSeconds(workflowTimeoutSeconds))
                .build();
    }
}
