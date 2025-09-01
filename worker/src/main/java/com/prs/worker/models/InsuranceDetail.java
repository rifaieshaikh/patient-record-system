package com.prs.worker.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.prs.dtos.InsuranceDetailDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InsuranceDetail {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonAlias("_id")
    private String id;
    private String providerName;
    private String policyNumber;
    private String coverageType;
    private Double coverageLimit;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String claimContact;

    public static InsuranceDetail from(InsuranceDetailDto insuranceDetailDto) {
        InsuranceDetail insuranceDetail = new InsuranceDetail();
        insuranceDetail.setClaimContact(insuranceDetailDto.claimContact());
        insuranceDetail.setProviderName(insuranceDetailDto.providerName());
        insuranceDetail.setPolicyNumber(insuranceDetailDto.policyNumber());
        insuranceDetail.setCoverageType(insuranceDetailDto.coverageType());
        insuranceDetail.setCoverageLimit(insuranceDetailDto.coverageLimit());
        insuranceDetail.setValidFrom(insuranceDetailDto.validFrom());
        insuranceDetail.setValidTo(insuranceDetailDto.validTo());
        return insuranceDetail;
    }
}
