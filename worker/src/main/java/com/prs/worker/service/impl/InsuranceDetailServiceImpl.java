package com.prs.worker.service.impl;

import com.prs.worker.dao.InsuranceDetailRepository;
import com.prs.worker.models.InsuranceDetail;
import com.prs.worker.service.InsuranceDetailService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.temporal.failure.ApplicationFailure;
import org.springframework.stereotype.Service;

import static com.prs.worker.utils.ErrorUtils.isClientError;
import static com.prs.worker.utils.ErrorUtils.messageFrom;

@Service
public class InsuranceDetailServiceImpl implements InsuranceDetailService {
    
    private final InsuranceDetailRepository insuranceDetailRepository;

    public InsuranceDetailServiceImpl(InsuranceDetailRepository insuranceDetailRepository) {
        this.insuranceDetailRepository = insuranceDetailRepository;
    }

    @Override
    @CircuitBreaker(name = "insuranceDetailApi", fallbackMethod = "createFallback")
    public InsuranceDetail create(InsuranceDetail insuranceDetail) {
        return insuranceDetailRepository.create(insuranceDetail);
    }

    @Override
    @CircuitBreaker(name = "insuranceDetailApi", fallbackMethod = "getFallback")
    public InsuranceDetail get(String id) {
        return insuranceDetailRepository.get(id);
    }

    @Override
    @CircuitBreaker(name = "insuranceDetailApi", fallbackMethod = "updateFallback")
    public void update(String id, InsuranceDetail insuranceDetail) {
        insuranceDetailRepository.update(id, insuranceDetail);
    }

    @Override
    @CircuitBreaker(name = "insuranceDetailApi", fallbackMethod = "deleteFallback")
    public void delete(String id) {
        insuranceDetailRepository.delete(id);
    }

    public void deleteFallback(String id, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling insuranceDetail service for %s", id), cause);
    }

    public void updateFallback(String id, InsuranceDetail record, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling insuranceDetail service for %s", id), cause);
    }


    public InsuranceDetail getFallback(String id, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException(String.format("Transient error calling insuranceDetail service for %s", id), cause);
    }

    public InsuranceDetail createFallback(InsuranceDetail record, Throwable cause) {
        if (isClientError(cause)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    messageFrom(cause), "ClientError");
        }
        throw new RuntimeException("Transient error calling insuranceDetail service", cause);
    }
}
