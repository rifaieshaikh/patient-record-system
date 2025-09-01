package com.prs.worker.service.impl;

import com.prs.worker.dao.MedicalRecordRepository;
import com.prs.worker.models.MedicalRecord;
import com.prs.worker.service.MedicalRecordService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.temporal.failure.ApplicationFailure;
import org.springframework.stereotype.Service;

import static com.prs.worker.utils.ErrorUtils.isClientError;
import static com.prs.worker.utils.ErrorUtils.messageFrom;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecordServiceImpl(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Override
    @CircuitBreaker(name = "medicalRecordApi", fallbackMethod = "createFallback")
    public MedicalRecord create(MedicalRecord MedicalRecord) {
        return medicalRecordRepository.create(MedicalRecord);
    }

    @Override
    @CircuitBreaker(name = "medicalRecordApi", fallbackMethod = "getFallback")
    public MedicalRecord get(String id) {
        return medicalRecordRepository.get(id);
    }

    @Override
    @CircuitBreaker(name = "medicalRecordApi", fallbackMethod = "updateFallback")
    public void update(String id, MedicalRecord medicalRecord) {
        medicalRecordRepository.update(id, medicalRecord);
    }

    @Override
    @CircuitBreaker(name = "medicalRecordApi", fallbackMethod = "deleteFallback")
    public void delete(String id) {
        medicalRecordRepository.delete(id);
    }

    public void deleteFallback(String id, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling medicalRecord service for %s", id), cause);
    }

    public void updateFallback(String id, MedicalRecord record, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling medicalRecord service for %s", id), cause);
    }


    public MedicalRecord getFallback(String id, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling medicalRecord service for %s", id), cause);
    }

    public MedicalRecord createFallback(MedicalRecord record, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException("Transient error calling medicalRecord service", cause);
    }
}
