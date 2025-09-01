package com.prs.worker.activities.impl;

import com.prs.documents.TransactionRequests;
import com.prs.dtos.TransactionRequestDTO;
import com.prs.worker.activities.CreatePatientActivity;
import com.prs.worker.models.InsuranceDetail;
import com.prs.worker.models.MedicalRecord;
import com.prs.worker.models.PatientRecord;
import com.prs.worker.service.InsuranceDetailService;
import com.prs.worker.service.MedicalRecordService;
import com.prs.worker.service.PatientService;
import com.prs.worker.service.TransactionRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("CreatePatientActivityImpl")
@Slf4j
public class CreatePatientActivityImpl implements CreatePatientActivity {

    private final TransactionRequestService transactionRequestService;
    private final PatientService patientService;
    private final InsuranceDetailService insuranceDetailService;
    private final MedicalRecordService medicalRecordService;

    public CreatePatientActivityImpl(TransactionRequestService transactionRequestService, PatientService patientService, InsuranceDetailService insuranceDetailService, MedicalRecordService medicalRecordService) {
        this.transactionRequestService = transactionRequestService;
        this.patientService = patientService;
        this.insuranceDetailService = insuranceDetailService;
        this.medicalRecordService = medicalRecordService;
    }

    @Override
    public void createPatientRecord(String transactionRequestId) {
        log.info("createPatientActivityImpl createPatientRecord(PatientDto patient)");
        TransactionRequests transactionRequest = transactionRequestService.getTransactionRequest(transactionRequestId);
        TransactionRequestDTO payload = transactionRequest.getPayload();
        PatientRecord patientRecord = PatientRecord.from(payload.patient());
        PatientRecord createdPatientRecord = patientService.create(patientRecord);
        if (Objects.isNull(transactionRequest.getPatientCreateMetaData())) {
            transactionRequest.setPatientCreateMetaData(new TransactionRequests.PatientCreateMetaData());
        }
        transactionRequest.getPatientCreateMetaData().setCreatedPatientId(createdPatientRecord.getId());
        transactionRequestService.save(transactionRequest);
    }

    @Override
    public void rollbackPatientRecord(String transactionRequestId) {
        log.info("rollbackPatientActivityImpl rollbackPatientRecord(PatientDto patient)");
        TransactionRequests transactionRequest = transactionRequestService.getTransactionRequest(transactionRequestId);
        String createdPatientId = transactionRequest.getPatientCreateMetaData().getCreatedPatientId();
        patientService.delete(createdPatientId);
    }

    @Override
    public void createMedicalRecord(String transactionRequestId) {
        log.info("createMedicalRecordImpl createMedicalRecord(PatientDto patient)");
        TransactionRequests transactionRequest = transactionRequestService.getTransactionRequest(transactionRequestId);
        TransactionRequestDTO payload = transactionRequest.getPayload();
        MedicalRecord record = MedicalRecord.from(payload.patient().medicalRecord());
        MedicalRecord createdRecord = medicalRecordService.create(record);
        transactionRequest.getPatientCreateMetaData().setCreatedMedicalRecordId(createdRecord.getId());
        transactionRequestService.save(transactionRequest);
    }

    @Override
    public void rollbackMedicalRecord(String transactionRequestId) {
        log.info("rollbackMedicalRecordImpl rollbackMedicalRecord(String medicalRecordId)");
        TransactionRequests transactionRequest = transactionRequestService.getTransactionRequest(transactionRequestId);
        String createdRecordId = transactionRequest.getPatientCreateMetaData().getCreatedMedicalRecordId();
        medicalRecordService.delete(createdRecordId);
    }

    @Override
    public void createInsuranceRecord(String transactionRequestId) {
        log.info("createInsuranceImpl createInsurance(PatientDto patient)");
        TransactionRequests transactionRequest = transactionRequestService.getTransactionRequest(transactionRequestId);
        TransactionRequestDTO payload = transactionRequest.getPayload();
        InsuranceDetail record = InsuranceDetail.from(payload.patient().insuranceDetail());
        InsuranceDetail createdRecord = insuranceDetailService.create(record);
        transactionRequest.getPatientCreateMetaData().setCreatedInsuranceDetailId(createdRecord.getId());
        transactionRequestService.save(transactionRequest);
    }

    @Override
    public void rollbackInsuranceRecord(String transactionRequestId) {
        log.info("rollbackInsuranceImpl rollbackInsurance(String insuranceId)");
        TransactionRequests transactionRequest = transactionRequestService.getTransactionRequest(transactionRequestId);
        String createdRecordId = transactionRequest.getPatientCreateMetaData().getCreatedInsuranceDetailId();
        insuranceDetailService.delete(createdRecordId);
    }
}
