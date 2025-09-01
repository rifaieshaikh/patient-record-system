package com.prs.dtos;

import java.time.LocalDate;

public record InsuranceDetailDto(String providerName, String policyNumber, String coverageType, Double coverageLimit,
                                 LocalDate validFrom, LocalDate validTo, String claimContact) {
}
