package com.prs.worker.service.impl;

import com.prs.worker.dao.PatientRepository;
import com.prs.worker.models.PatientRecord;
import com.prs.worker.service.PatientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.temporal.failure.ApplicationFailure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.prs.worker.utils.ErrorUtils.isClientError;
import static com.prs.worker.utils.ErrorUtils.messageFrom;

@Service
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }


    @Override
    @CircuitBreaker(name = "patientApi", fallbackMethod = "createFallback")
    public PatientRecord create(PatientRecord patientRecord) {
        return patientRepository.create(patientRecord);
    }

    @Override
    @CircuitBreaker(name = "patientApi", fallbackMethod = "getFallback")
    public PatientRecord get(String id) {
        return patientRepository.get(id);
    }

    @Override
    @CircuitBreaker(name = "patientApi", fallbackMethod = "updateFallback")
    public void update(String id, PatientRecord patientRecord) {
        patientRepository.update(id, patientRecord);
    }

    @Override
    @CircuitBreaker(name = "patientApi", fallbackMethod = "deleteFallback")
    public void delete(String id) {
        patientRepository.delete(id);
    }

    public void deleteFallback(String id, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling patient service for %s", id), cause);
    }

    public void updateFallback(String id, PatientRecord patientRecord, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling patient service for %s", id), cause);
    }


    public PatientRecord getFallback(String id, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling patient service for %s", id), cause);
    }

    public PatientRecord createFallback(PatientRecord record, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException("Transient error calling patient service", cause);
    }
}
