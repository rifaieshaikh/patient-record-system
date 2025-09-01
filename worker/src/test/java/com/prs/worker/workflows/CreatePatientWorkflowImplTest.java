package com.prs.worker.workflows;


import com.prs.constans.TransactionStatus;
import com.prs.constans.TransactionType;
import com.prs.documents.TransactionRequests;
import com.prs.dtos.InsuranceDetailDto;
import com.prs.dtos.MedicalRecordDto;
import com.prs.dtos.PatientDto;
import com.prs.dtos.TransactionRequestDTO;
import com.prs.worker.activities.CreatePatientActivity;
import com.prs.worker.activities.TransactionRequestActivity;
import com.prs.workflow.CreatePatientWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.TimeoutFailure;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CreatePatientWorkflowImplTest {
    private TestWorkflowEnvironment testEnv;
    private Worker worker;
    private WorkflowClient client;

    @Mock
    private CreatePatientActivity createPatientActivity;

    @Mock
    private TransactionRequestActivity transactionRequestActivity;

    private static final String TASK_QUEUE = "CREATE_PATIENT_TASK_QUEUE";
    private static final String REQUEST_ID = "test-request-123";

    private PatientDto testPatientDto;
    private TransactionRequestDTO testTransactionRequestDTO;
    private TransactionRequests testTransactionRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testEnv = TestWorkflowEnvironment.newInstance();
        worker = testEnv.newWorker(TASK_QUEUE);
        client = testEnv.getWorkflowClient();

        worker.registerWorkflowImplementationTypes(CreatePatientWorkflowImpl.class);

        MedicalRecordDto medicalRecord = new MedicalRecordDto(
                "Sun light",
                "Diabetes",
                "Met",
                "NA",
                "Rahul"
        );

        InsuranceDetailDto insuranceDetail = new InsuranceDetailDto(
                "Tata Capital",
                "POL-101",
                "Seeta",
                Double.valueOf(100000),
                LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(1),
                "9897697697"
        );

        testPatientDto = new PatientDto(
                null,
                "Rohan",
                "Raaj",
                LocalDate.of(1990, 1, 1),
                "Male",
                "9897989898",
                "rohan.raaj@gmail.com",
                "Test house",
                "O+",
                medicalRecord,
                insuranceDetail
        );

        testTransactionRequestDTO = new TransactionRequestDTO(testPatientDto, TransactionType.CREATE);

        testTransactionRequest = new TransactionRequests();
        testTransactionRequest.setId(REQUEST_ID);
        testTransactionRequest.setTransactionType(TransactionType.CREATE);
        testTransactionRequest.setTransactionStatus(TransactionStatus.INITIATED);
        testTransactionRequest.setPayload(testTransactionRequestDTO);
        testTransactionRequest.setPatientCreateMetaData(new TransactionRequests.PatientCreateMetaData());
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }


    @Test
    void testSuccessfulPatientCreationWorkflow() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);
        doNothing().when(createPatientActivity).createPatientRecord(REQUEST_ID);
        doNothing().when(createPatientActivity).createMedicalRecord(REQUEST_ID);
        doNothing().when(createPatientActivity).createInsuranceRecord(REQUEST_ID);
        doNothing().when(transactionRequestActivity).markTransactionRequestSucceeded(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build()
        );

        WorkflowClient.start(workflow::createPatient, REQUEST_ID);

        testEnv.sleep(Duration.ofSeconds(1));

        verify(transactionRequestActivity, times(1)).markTransactionRequestInProgress(REQUEST_ID);
        verify(createPatientActivity, times(1)).createPatientRecord(REQUEST_ID);
        verify(createPatientActivity, times(1)).createMedicalRecord(REQUEST_ID);
        verify(createPatientActivity, times(1)).createInsuranceRecord(REQUEST_ID);
        verify(transactionRequestActivity, times(1)).markTransactionRequestSucceeded(REQUEST_ID);

        verify(createPatientActivity, never()).rollbackPatientRecord(anyString());
        verify(createPatientActivity, never()).rollbackMedicalRecord(anyString());
        verify(createPatientActivity, never()).rollbackInsuranceRecord(anyString());
    }

    @Test
    void testPatientCreationFailsAtFirstStep_NoRollbackNeeded() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);
        doThrow(ApplicationFailure.newFailure("Database connection failed", "DB_ERROR"))
                .when(createPatientActivity).createPatientRecord(REQUEST_ID);
        doNothing().when(transactionRequestActivity).markTransactionRequestFailed(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-fail-first-" + System.currentTimeMillis())
                        .build()
        );

        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.start(REQUEST_ID);

        assertThatThrownBy(() -> {
            untyped.getResult(Void.class);
        }).isInstanceOf(io.temporal.client.WorkflowFailedException.class)
                .hasCauseInstanceOf(ActivityFailure.class);

        verify(transactionRequestActivity, times(1)).markTransactionRequestInProgress(REQUEST_ID);
        verify(createPatientActivity, atLeastOnce()).createPatientRecord(REQUEST_ID);

        verify(createPatientActivity, never()).rollbackPatientRecord(anyString());
        verify(createPatientActivity, never()).rollbackMedicalRecord(anyString());
        verify(createPatientActivity, never()).rollbackInsuranceRecord(anyString());
    }

    @Test
    void testPatientCreationFailsAtSecondStep_RollbackFirstStep() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);
        doNothing().when(createPatientActivity).createPatientRecord(REQUEST_ID);
        doThrow(ApplicationFailure.newFailure("Medical service unavailable", "SERVICE_ERROR"))
                .when(createPatientActivity).createMedicalRecord(REQUEST_ID);
        doNothing().when(createPatientActivity).rollbackPatientRecord(REQUEST_ID);
        doNothing().when(transactionRequestActivity).markTransactionRequestFailed(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-fail-second-" + System.currentTimeMillis())
                        .build()
        );

        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.start(REQUEST_ID);

        assertThatThrownBy(() -> {
            untyped.getResult(Void.class);
        }).isInstanceOf(io.temporal.client.WorkflowFailedException.class)
                .hasCauseInstanceOf(ActivityFailure.class);

        verify(transactionRequestActivity, times(1)).markTransactionRequestInProgress(REQUEST_ID);
        verify(createPatientActivity, times(1)).createPatientRecord(REQUEST_ID);
        verify(createPatientActivity, atLeastOnce()).createMedicalRecord(REQUEST_ID);

        verify(createPatientActivity, times(1)).rollbackPatientRecord(REQUEST_ID);
        verify(createPatientActivity, never()).rollbackMedicalRecord(anyString());
        verify(createPatientActivity, never()).rollbackInsuranceRecord(anyString());
    }

    @Test
    void testPatientCreationFailsAtThirdStep_RollbackPreviousSteps() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);
        doNothing().when(createPatientActivity).createPatientRecord(REQUEST_ID);
        doNothing().when(createPatientActivity).createMedicalRecord(REQUEST_ID);
        doThrow(ApplicationFailure.newFailure("Insurance validation failed", "VALIDATION_ERROR"))
                .when(createPatientActivity).createInsuranceRecord(REQUEST_ID);
        doNothing().when(createPatientActivity).rollbackPatientRecord(REQUEST_ID);
        doNothing().when(createPatientActivity).rollbackMedicalRecord(REQUEST_ID);
        doNothing().when(transactionRequestActivity).markTransactionRequestFailed(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-fail-third-" + System.currentTimeMillis())
                        .build()
        );

        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.start(REQUEST_ID);

        assertThatThrownBy(() -> {
            untyped.getResult(Void.class);
        }).isInstanceOf(io.temporal.client.WorkflowFailedException.class)
                .hasCauseInstanceOf(ActivityFailure.class);

        verify(transactionRequestActivity, times(1)).markTransactionRequestInProgress(REQUEST_ID);
        verify(createPatientActivity, times(1)).createPatientRecord(REQUEST_ID);
        verify(createPatientActivity, times(1)).createMedicalRecord(REQUEST_ID);
        verify(createPatientActivity, atLeastOnce()).createInsuranceRecord(REQUEST_ID);

        verify(createPatientActivity, times(1)).rollbackMedicalRecord(REQUEST_ID);
        verify(createPatientActivity, times(1)).rollbackPatientRecord(REQUEST_ID);
        verify(createPatientActivity, never()).rollbackInsuranceRecord(anyString());
    }

    @Test
    void testWorkflowTimeout() {
        CreatePatientActivity slowActivity = mock(CreatePatientActivity.class);
        TransactionRequestActivity transactionActivity = mock(TransactionRequestActivity.class);

        doAnswer(invocation -> {
            Thread.sleep(30000); // Sleep for 30 seconds to trigger timeout
            return null;
        }).when(slowActivity).createPatientRecord(anyString());

        worker.registerActivitiesImplementations(slowActivity, transactionActivity);
        testEnv.start();

        WorkflowClient client = testEnv.getWorkflowClient();
        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowExecutionTimeout(Duration.ofSeconds(5)) // Set short timeout
                        .build()
        );

        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.start(REQUEST_ID);

        assertThatThrownBy(() -> {
            untyped.getResult(Void.class);
        }).isInstanceOf(io.temporal.client.WorkflowFailedException.class)
                .hasCauseInstanceOf(TimeoutFailure.class);
    }

    @Test
    void testRetryMechanismOnTransientFailure() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);

        doThrow(ApplicationFailure.newFailure("Temporary network issue", "NETWORK_ERROR"))
                .doNothing()
                .when(createPatientActivity).createPatientRecord(REQUEST_ID);

        doNothing().when(createPatientActivity).createMedicalRecord(REQUEST_ID);
        doNothing().when(createPatientActivity).createInsuranceRecord(REQUEST_ID);
        doNothing().when(transactionRequestActivity).markTransactionRequestSucceeded(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build()
        );

        WorkflowClient.start(workflow::createPatient, REQUEST_ID);
        testEnv.sleep(Duration.ofSeconds(3));

        verify(transactionRequestActivity, times(1)).markTransactionRequestInProgress(REQUEST_ID);
        verify(createPatientActivity, times(2)).createPatientRecord(REQUEST_ID);
        verify(createPatientActivity, times(1)).createMedicalRecord(REQUEST_ID);
        verify(createPatientActivity, times(1)).createInsuranceRecord(REQUEST_ID);
        verify(transactionRequestActivity, times(1)).markTransactionRequestSucceeded(REQUEST_ID);

        verify(createPatientActivity, never()).rollbackPatientRecord(anyString());
        verify(createPatientActivity, never()).rollbackMedicalRecord(anyString());
        verify(createPatientActivity, never()).rollbackInsuranceRecord(anyString());
    }

    @Test
    void testRollbackFailureHandling() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);
        doNothing().when(createPatientActivity).createPatientRecord(REQUEST_ID);
        doThrow(ApplicationFailure.newFailure("Medical service error", "SERVICE_ERROR"))
                .when(createPatientActivity).createMedicalRecord(REQUEST_ID);

        doThrow(ApplicationFailure.newFailure("Rollback failed", "ROLLBACK_ERROR"))
                .when(createPatientActivity).rollbackPatientRecord(REQUEST_ID);
        doNothing().when(transactionRequestActivity).markTransactionRequestFailed(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-rollback-fail-" + System.currentTimeMillis())
                        .build()
        );

        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.start(REQUEST_ID);

        assertThatThrownBy(() -> {
            untyped.getResult(Void.class);
        }).isInstanceOf(io.temporal.client.WorkflowFailedException.class)
                .hasCauseInstanceOf(ActivityFailure.class);

        verify(transactionRequestActivity, times(1)).markTransactionRequestInProgress(REQUEST_ID);
        verify(createPatientActivity, times(1)).createPatientRecord(REQUEST_ID);
        verify(createPatientActivity, atLeastOnce()).createMedicalRecord(REQUEST_ID);

        verify(createPatientActivity, atLeastOnce()).rollbackPatientRecord(REQUEST_ID);
    }

    @Test
    void testMarkTransactionFailedHandling() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);
        doThrow(ApplicationFailure.newFailure("Database error", "DB_ERROR"))
                .when(createPatientActivity).createPatientRecord(REQUEST_ID);
        doThrow(ApplicationFailure.newFailure("Cannot update transaction", "UPDATE_ERROR"))
                .when(transactionRequestActivity).markTransactionRequestFailed(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-mark-fail-" + System.currentTimeMillis())
                        .build()
        );

        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.start(REQUEST_ID);

        assertThatThrownBy(() -> {
            untyped.getResult(Void.class);
        }).isInstanceOf(io.temporal.client.WorkflowFailedException.class)
                .hasCauseInstanceOf(ActivityFailure.class);

        verify(transactionRequestActivity, times(1)).markTransactionRequestInProgress(REQUEST_ID);
        verify(createPatientActivity, atLeastOnce()).createPatientRecord(REQUEST_ID);
        verify(transactionRequestActivity, atLeastOnce()).markTransactionRequestFailed(REQUEST_ID);
    }

    @Test
    void testMaxRetryAttemptsExceeded() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);
        doThrow(ApplicationFailure.newFailure("Persistent failure", "PERSISTENT_ERROR"))
                .when(createPatientActivity).createPatientRecord(REQUEST_ID);
        doNothing().when(transactionRequestActivity).markTransactionRequestFailed(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-max-retry-" + System.currentTimeMillis())
                        .build()
        );

        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.start(REQUEST_ID);

        assertThatThrownBy(() -> {
            testEnv.sleep(Duration.ofMinutes(2));
            untyped.getResult(Void.class);
        }).isInstanceOf(io.temporal.client.WorkflowFailedException.class)
                .hasCauseInstanceOf(ActivityFailure.class);

        verify(createPatientActivity, atLeast(5)).createPatientRecord(REQUEST_ID);
        verify(transactionRequestActivity, atLeastOnce()).markTransactionRequestFailed(REQUEST_ID);
    }

    @Test
    void testNonRetryableFailure() {
        worker.registerActivitiesImplementations(createPatientActivity, transactionRequestActivity);
        testEnv.start();

        doNothing().when(transactionRequestActivity).markTransactionRequestInProgress(REQUEST_ID);
        doThrow(ApplicationFailure.newNonRetryableFailure("Invalid patient data", "VALIDATION_ERROR"))
                .when(createPatientActivity).createPatientRecord(REQUEST_ID);
        doNothing().when(transactionRequestActivity).markTransactionRequestFailed(REQUEST_ID);

        CreatePatientWorkflow workflow = client.newWorkflowStub(
                CreatePatientWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-non-retry-" + System.currentTimeMillis())
                        .build()
        );

        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.start(REQUEST_ID);

        assertThatThrownBy(() -> {
            untyped.getResult(Void.class);
        }).isInstanceOf(io.temporal.client.WorkflowFailedException.class)
                .hasCauseInstanceOf(ActivityFailure.class);

        verify(createPatientActivity, times(1)).createPatientRecord(REQUEST_ID);
        verify(transactionRequestActivity, atLeastOnce()).markTransactionRequestFailed(REQUEST_ID);
    }
}