package com.prs.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PatientDto(String id, @NotBlank String firstName, @NotBlank String lastName, @Past LocalDate dateOfBirth, @NotBlank String gender, @NotBlank String phoneNumber,
                         String email, String address, String bloodGroup, MedicalRecordDto medicalRecord,
                         InsuranceDetailDto insuranceDetail) {

}
